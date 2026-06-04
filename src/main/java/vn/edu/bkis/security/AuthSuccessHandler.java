package vn.edu.bkis.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import vn.edu.bkis.constan.ConstantCommon;
import vn.edu.bkis.repository.UserRepository;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    /**
     * Khoi tao handler xu ly sau khi nguoi dung dang nhap thanh cong.
     *
     * @param userRepository repository dung de cap nhat trang thai dang nhap sai cua user local
     * @return khong tra du lieu; constructor dung de gan dependency cho handler
     */
    public AuthSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Xu ly redirect sau dang nhap thanh cong va uu tien quay lai URL ma nguoi dung dang truy cap do.
     *
     * @param request request hien tai chua session va saved request truoc login
     * @param response response dung de redirect nguoi dung sau khi xac thuc xong
     * @param authentication thong tin xac thuc thanh cong cua nguoi dung hien tai
     * @return khong tra du lieu; method ghi redirect truc tiep vao response
     * @throws IOException nem ra khi response khong the ghi redirect
     * @throws ServletException nem ra khi container servlet bao loi trong luong xac thuc
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Step 1: dong bo lai trang thai tai khoan local sau khi nguoi dung xac thuc thanh cong.
        Object principal = authentication.getPrincipal();
        vn.edu.bkis.model.User authenticatedUser = null;
        if (principal instanceof CustomUserDetails customUserDetails) {
            authenticatedUser = customUserDetails.getUser();
            resetLoginAttempts(authenticatedUser);
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            authenticatedUser = customOAuth2User.getUser();
            resetLoginAttempts(authenticatedUser);
        }

        // Step 2: neu day la tai khoan SA thi uu tien dua thang vao dashboard admin.
        if (isSystemAdminAccount(authenticatedUser)) {
            response.sendRedirect("/admin/dashboard");
            return;
        }

        // Step 3: doc URL ma Spring Security da luu truoc khi day nguoi dung sang trang login.
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            requestCache.removeRequest(request, response);
            String redirectUrl = resolveRedirectUrl(savedRequest);
            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
                return;
            }
        }

        // Step 4: neu khong co saved request hop le thi fallback ve trang chu.
        response.sendRedirect("/");
    }

    /**
     * Dat lai bo dem dang nhap sai va mo khoa tai khoan local sau khi xac thuc thanh cong.
     *
     * @param user thuc the user local can cap nhat trang thai dang nhap
     * @return khong tra du lieu; method luu truc tiep trang thai moi cua user xuong database
     */
    private void resetLoginAttempts(vn.edu.bkis.model.User user) {
        // Step 1: dua bo dem dang nhap sai ve 0 va mo khoa neu tai khoan tung bi khoa tam thoi.
        user.setFailedLoginAttempts(ConstantCommon.ZERO_NUMBER);
        user.setLocked(false);

        // Step 2: luu trang thai tai khoan moi de cac lan dang nhap sau khong bi anh huong.
        userRepository.save(user);
    }

    /**
     * Kiem tra tai khoan dang nhap co phai tai khoan SA duoc dung cho dashboard admin hay khong.
     *
     * @param user user local da dang nhap thanh cong
     * @return true neu day la tai khoan SA; nguoc lai tra ve false
     */
    private boolean isSystemAdminAccount(vn.edu.bkis.model.User user) {
        // Step 1: chan som truong hop khong co user local sau khi xac thuc.
        if (user == null || user.getUsername() == null) {
            return false;
        }

        // Step 2: doi chieu username voi tai khoan SA duoc seed san trong he thong.
        return "SA".equalsIgnoreCase(user.getUsername().trim());
    }

    /**
     * Chuan hoa URL redirect sau login va loai bo cac dich khong an toan hoac khong hop le.
     *
     * @param savedRequest saved request ma Spring Security luu lai truoc khi buoc nguoi dung dang nhap
     * @return {@link String} URL redirect hop le; tra ve null neu can fallback ve trang chu
     */
    private String resolveRedirectUrl(SavedRequest savedRequest) {
        // Step 1: doc URL goc tu saved request va chan som cac gia tri rong.
        String redirectUrl = savedRequest.getRedirectUrl();
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return null;
        }

        // Step 2: parse URL de kiem tra path va query, dong thoi loai bo dich quay vong ve /error?continue.
        URI redirectUri = URI.create(redirectUrl);
        String path = redirectUri.getPath();
        String query = redirectUri.getQuery();
        if (path == null || path.isBlank()) {
            return null;
        }
        if ("/error".equals(path) || (query != null && query.contains("continue"))) {
            return null;
        }

        // Step 3: tra lai URL hop le de user quay lai man hinh dang truy cap do.
        return redirectUrl;
    }
}
