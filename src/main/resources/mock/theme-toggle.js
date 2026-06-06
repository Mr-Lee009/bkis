(function () {
    const STORAGE_KEY = "mock_ui_theme";
    const JQUERY_CDN = "https://code.jquery.com/jquery-3.7.1.min.js";

    function init($) {
        function currentTheme() {
            return $("body").attr("data-theme") === "dark" ? "dark" : "light";
        }

        function renderToggle(theme) {
            const $button = $("#mockThemeToggle");
            if (!$button.length) {
                return;
            }

            if (theme === "dark") {
                $button
                    .html('<i class="fa fa-sun me-2"></i>Light mode')
                    .removeClass("btn-dark")
                    .addClass("btn-light");
                return;
            }

            $button
                .html('<i class="fa fa-moon me-2"></i>Dark mode')
                .removeClass("btn-light")
                .addClass("btn-dark");
        }

        function applyTheme(theme) {
            const nextTheme = theme === "dark" ? "dark" : "light";
            $("body").attr("data-theme", nextTheme);
            localStorage.setItem(STORAGE_KEY, nextTheme);
            renderToggle(nextTheme);
        }

        function injectToggle() {
            if ($("#mockThemeToggle").length) {
                return;
            }

            const html = [
                '<div class="theme-toggle-floating">',
                '  <button id="mockThemeToggle" type="button" class="btn btn-dark shadow-sm"></button>',
                "</div>"
            ].join("");

            $("body").append(html);
        }

        $(function () {
            injectToggle();
            applyTheme(localStorage.getItem(STORAGE_KEY) || "light");

            $(document).on("click", "#mockThemeToggle", function () {
                applyTheme(currentTheme() === "dark" ? "light" : "dark");
            });
        });
    }

    if (window.jQuery) {
        init(window.jQuery);
        return;
    }

    const script = document.createElement("script");
    script.src = JQUERY_CDN;
    script.onload = function () {
        init(window.jQuery);
    };
    document.head.appendChild(script);
})();
