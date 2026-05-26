package vn.edu.bkis.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class CaptchaService {
    // Session key used to store the expected captcha answer for the current browser session.
    private static final String CAPTCHA_SESSION_KEY = "captchaExpected";

    // Number of characters rendered into each captcha image.
    private static final int CAPTCHA_LENGTH = 5;

    // Image size tuned for the login form so the captcha stays readable but compact.
    private static final int CAPTCHA_WIDTH = 150;
    private static final int CAPTCHA_HEIGHT = 46;

    // Allowed characters exclude ambiguous symbols such as I, O, 0, and 1.
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    // Cryptographically strong random source used for captcha text, noise, and line placement.
    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a fresh captcha image and stores its expected answer in the HTTP session.
     *
     * @param session current user's HTTP session, used to persist the expected captcha answer
     * @return PNG image bytes that can be written directly to the HTTP response
     */
    public byte[] generateCaptchaImage(HttpSession session) {
        // Random text that the user must type back in the login form.
        String captchaText = generateCaptchaText();
        session.setAttribute(CAPTCHA_SESSION_KEY, captchaText);

        // In-memory image buffer that will be returned to the browser as PNG.
        BufferedImage image = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Drawing context used to paint background, noise, text, and interference lines.
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(240, 251, 252));
            graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT);
            drawNoise(graphics);
            drawCaptchaText(graphics, captchaText);
            drawInterferenceLines(graphics);
        } finally {
            graphics.dispose();
        }

        // Byte output buffer holding the final PNG payload returned by the controller.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to render captcha image", ex);
        }
    }

    /**
     * Compares the user's captcha answer against the value stored in session.
     * The session value is always removed after the check to prevent replay.
     *
     * @param answer raw captcha value submitted by the user from the login form
     * @param session current user's HTTP session holding the expected captcha answer
     * @return true when the provided answer matches the stored captcha value, ignoring case
     */
    public boolean matches(String answer, HttpSession session) {
        // Previously generated captcha value stored during image generation.
        Object expected = session.getAttribute(CAPTCHA_SESSION_KEY);
        session.removeAttribute(CAPTCHA_SESSION_KEY);
        if (answer == null || expected == null) {
            return false;
        }
        return answer.trim().equalsIgnoreCase(expected.toString());
    }

    /**
     * Builds a random captcha string using the allowed character set.
     *
     * @return random captcha text with fixed length
     */
    private String generateCaptchaText() {
        // Mutable builder used to assemble the final captcha text.
        StringBuilder builder = new StringBuilder(CAPTCHA_LENGTH);

        // Character index inside the captcha text currently being generated.
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            builder.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return builder.toString();
    }

    /**
     * Draws captcha characters with slight rotation and vertical offsets to reduce OCR accuracy.
     *
     * @param graphics drawing context for the captcha image
     * @param captchaText captcha text to render into the image
     */
    private void drawCaptchaText(Graphics2D graphics, String captchaText) {
        graphics.setFont(new Font("Arial", Font.BOLD, 24));

        // Character index currently being drawn from the captcha string.
        for (int i = 0; i < captchaText.length(); i++) {
            // Horizontal position of the current character.
            int x = 16 + (i * 24);

            // Vertical baseline of the current character, with a small random offset.
            int y = 31 + random.nextInt(7);

            // Original transform restored after drawing the rotated character.
            AffineTransform originalTransform = graphics.getTransform();
            graphics.rotate(Math.toRadians(random.nextInt(31) - 15), x, y);
            graphics.setColor(randomDarkColor());
            graphics.drawString(String.valueOf(captchaText.charAt(i)), x, y);
            graphics.setTransform(originalTransform);
        }
    }

    /**
     * Draws small light-colored dots across the image to make text extraction harder.
     *
     * @param graphics drawing context for the captcha image
     */
    private void drawNoise(Graphics2D graphics) {
        // Noise element counter controlling how many random dots are painted.
        for (int i = 0; i < 90; i++) {
            graphics.setColor(randomLightColor());

            // Top-left X position of the current noise dot.
            int x = random.nextInt(CAPTCHA_WIDTH);

            // Top-left Y position of the current noise dot.
            int y = random.nextInt(CAPTCHA_HEIGHT);

            // Width of the current noise dot.
            int width = random.nextInt(3) + 1;

            // Height of the current noise dot.
            int height = random.nextInt(3) + 1;
            graphics.fillOval(x, y, width, height);
        }
    }

    /**
     * Draws crossing lines over the captcha image to reduce machine readability.
     *
     * @param graphics drawing context for the captcha image
     */
    private void drawInterferenceLines(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(1.5f));

        // Line counter controlling how many interference lines are drawn.
        for (int i = 0; i < 6; i++) {
            graphics.setColor(randomDarkColor());

            // X coordinate of the line start point.
            int x1 = random.nextInt(CAPTCHA_WIDTH / 3);

            // Y coordinate of the line start point.
            int y1 = random.nextInt(CAPTCHA_HEIGHT);

            // X coordinate of the line end point.
            int x2 = CAPTCHA_WIDTH - random.nextInt(CAPTCHA_WIDTH / 3);

            // Y coordinate of the line end point.
            int y2 = random.nextInt(CAPTCHA_HEIGHT);
            graphics.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * Creates a random dark color for captcha foreground elements.
     *
     * @return dark RGB color used for characters and interference lines
     */
    private Color randomDarkColor() {
        return new Color(20 + random.nextInt(90), 30 + random.nextInt(100), 60 + random.nextInt(100));
    }

    /**
     * Creates a random light color for background noise dots.
     *
     * @return light RGB color used for subtle background noise
     */
    private Color randomLightColor() {
        return new Color(150 + random.nextInt(70), 180 + random.nextInt(50), 190 + random.nextInt(50));
    }
}
