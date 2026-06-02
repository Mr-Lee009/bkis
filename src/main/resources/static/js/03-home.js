(function ($) {
    "use strict";

    // Khởi tạo slider tin khóa học riêng cho trang chủ với 1 item ở mobile và 2 item ở desktop.
    $(".hot-news-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 900,
        margin: 24,
        dots: true,
        loop: true,
        nav: true,
        navText: [
            '<i class="bi bi-chevron-left"></i>',
            '<i class="bi bi-chevron-right"></i>'
        ],
        responsive: {
            0: {
                items: 1
            },
            992: {
                items: 2
            }
        }
    });

    // Khởi tạo slider cảm nhận học viên chỉ dùng ở trang chủ.
    $(".testimonial-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 1000,
        center: true,
        margin: 24,
        dots: true,
        loop: true,
        nav: false,
        responsive: {
            0: {
                items: 1
            },
            768: {
                items: 2
            },
            992: {
                items: 3
            }
        }
    });
})(jQuery);
