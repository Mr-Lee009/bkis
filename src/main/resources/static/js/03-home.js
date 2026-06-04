(function ($) {
    "use strict";

    // Khoi tao slider tin khoa hoc rieng cho trang chu voi 1 item o mobile va 2 item o desktop.
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

    // Khoi tao slider cam nhan hoc vien chi dung o trang chu.
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
