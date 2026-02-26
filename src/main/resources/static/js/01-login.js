$(function () {
    var $captchaTextEl = $('#captchaText');
    var $captchaInput = $('#captchaInput');
    var $refreshBtn = $('#refreshCaptcha');
    var $togglePassword = $('#togglePassword');
    var $passwordInput = $('#passwordInput');
    var $loginForm = $('#loginForm');

    function generateCaptcha() {
        var chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
        var output = '';
        for (var i = 0; i < 5; i++) {
            output += chars[Math.floor(Math.random() * chars.length)];
        }
        $captchaTextEl.text(output);
        $captchaInput.val('');
    }

    $refreshBtn.on('click', function () {
        generateCaptcha();
    });

    $togglePassword.on('click', function () {
        var isHidden = $passwordInput.attr('type') === 'password';
        $passwordInput.attr('type', isHidden ? 'text' : 'password');
        $(this).html(isHidden ? '<i class="fa fa-eye-slash"></i>' : '<i class="fa fa-eye"></i>');
    });

    $loginForm.on('submit', function (event) {
        if ($.trim($captchaInput.val()).toUpperCase() !== $.trim($captchaTextEl.text())) {
            event.preventDefault();
            $captchaInput.addClass('is-invalid');
            $captchaInput.focus();
        } else {
            $captchaInput.removeClass('is-invalid');
            alert('Xác thực thành công!');
        }
    });

    $captchaInput.on('input', function () {
        $(this).removeClass('is-invalid');
    });

    generateCaptcha();
});