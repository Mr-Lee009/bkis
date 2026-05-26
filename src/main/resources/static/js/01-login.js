$(function () {
    // Captcha image element rendered from the server-side /captcha/image endpoint.
    var $captchaImage = $('#captchaImage');

    // Input where the user types the captcha text they see in the image.
    var $captchaInput = $('#captchaInput');

    // Refresh button used to request a brand-new captcha image.
    var $refreshBtn = $('#refreshCaptcha');

    // Button used to toggle password visibility in the login form.
    var $togglePassword = $('#togglePassword');

    // Password input field whose type switches between password and text.
    var $passwordInput = $('#passwordInput');

    // Reloads the captcha image with a timestamp query string to bypass browser cache.
    function refreshCaptcha() {
        $captchaImage.attr('src', '/captcha/image?ts=' + Date.now());
        $captchaInput.val('').removeClass('is-invalid');
    }

    $refreshBtn.on('click', function () {
        refreshCaptcha();
    });

    $togglePassword.on('click', function () {
        var isHidden = $passwordInput.attr('type') === 'password';
        $passwordInput.attr('type', isHidden ? 'text' : 'password');
        $(this).html(isHidden ? '<i class="fa fa-eye-slash"></i>' : '<i class="fa fa-eye"></i>');
    });

    $captchaInput.on('input', function () {
        $(this).removeClass('is-invalid');
    });

    $captchaImage.on('dragstart contextmenu', function (event) {
        event.preventDefault();
    });
});
