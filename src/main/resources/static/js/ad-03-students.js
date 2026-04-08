$(function () {
    const $keyword = $('#studentKeyword');
    const $pageSize = $('#studentPageSize');
    const $searchButton = $('#studentSearchButton');
    const $resetButton = $('#studentResetButton');
    const $tableBody = $('#studentTableBody');
    const $pagination = $('#studentPagination');
    const $paginationSummary = $('#studentPaginationSummary');
    const $apiError = $('#studentApiError');
    const $activeCount = $('#studentActiveCount');
    const $onboardingCount = $('#studentOnboardingCount');
    const $openTicketCount = $('#studentOpenTicketCount');
    const $openTicketBadge = $('#studentOpenTicketBadge');
    const $currentPage = $('#studentCurrentPage');
    const $pageSizeCard = $('#studentPageSizeCard');
    const $statusPills = $('.student-status-pill');

    const state = {
        page: 0,
        size: Number($pageSize.val()) || 10,
        keyword: '',
        status: ''
    };

    const escapeHtml = (value = '') => String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');

    const formatDateTime = (value) => {
        if (!value) {
            return 'N/A';
        }

        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return date.toLocaleString('vi-VN');
    };

    const renderRows = (items) => {
        if (!items.length) {
            $tableBody.html(`
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">Không có học viên phù hợp.</td>
                </tr>
            `);
            return;
        }

        const rowsHtml = items.map((item) => {
            const progressPercent = Number(item.progressPercent) || 0;
            const progressClass = progressPercent >= 100
                ? 'bg-primary'
                : progressPercent >= 50
                    ? 'bg-success'
                    : progressPercent >= 25
                        ? 'bg-warning'
                        : 'bg-info';
            const progressLabel = escapeHtml(item.progressLabel || 'Chưa có dữ liệu');
            const statusClass = item.status === 'ACTIVE'
                ? 'bg-success'
                : item.status === 'ONBOARDING'
                    ? 'bg-info text-dark'
                    : 'bg-light text-dark';

            return `
                <tr class="student-row">
                    <td>
                        <strong>${escapeHtml(item.fullName || '')}</strong>
                        <div class="text-muted small">${escapeHtml(item.email || '')}</div>
                    </td>
                    <td>${escapeHtml(item.courseName || 'Chưa ghi danh')}</td>
                    <td>${escapeHtml(item.cohortCode || '--')}</td>
                    <td style="min-width:140px;">
                        <div class="d-flex justify-content-between small">
                            <span>${progressPercent}%</span>
                            <span>${progressLabel}</span>
                        </div>
                        <div class="progress progress-mini bg-light">
                            <div class="progress-bar ${progressClass}" style="width:${progressPercent}%;"></div>
                        </div>
                    </td>
                    <td><span class="badge ${statusClass}">${escapeHtml(item.statusLabel || item.status || '')}</span></td>
                    <td class="text-end">
                        <button type="button" class="btn btn-sm btn-outline-secondary me-2" disabled>Chi tiết</button>
                        <button type="button" class="btn btn-sm btn-outline-primary" disabled>Gửi nhắc</button>
                    </td>
                </tr>
            `;
        }).join('');

        $tableBody.html(rowsHtml);
    };

    const renderPagination = (pageData) => {
        $pagination.empty();

        if (!pageData.totalPages || pageData.totalPages <= 1) {
            return;
        }

        const prevDisabled = pageData.hasPrevious ? '' : ' disabled';
        const nextDisabled = pageData.hasNext ? '' : ' disabled';

        $pagination.append(`
            <li class="page-item${prevDisabled}">
                <button class="page-link" type="button" data-page="${pageData.page - 1}">«</button>
            </li>
        `);

        for (let i = 0; i < pageData.totalPages; i += 1) {
            const active = i === pageData.page ? ' active' : '';
            $pagination.append(`
                <li class="page-item${active}">
                    <button class="page-link" type="button" data-page="${i}">${i + 1}</button>
                </li>
            `);
        }

        $pagination.append(`
            <li class="page-item${nextDisabled}">
                <button class="page-link" type="button" data-page="${pageData.page + 1}">»</button>
            </li>
        `);
    };

    const renderSummary = (pageData) => {
        const startRow = pageData.totalElements === 0 ? 0 : (pageData.page * pageData.size) + 1;
        const endRow = Math.min(pageData.totalElements, (pageData.page + 1) * pageData.size);

        $paginationSummary.text(`Hiển thị ${startRow}-${endRow} / ${pageData.totalElements} học viên`);
        $currentPage.text(pageData.totalPages === 0 ? 0 : pageData.page + 1);
        $pageSizeCard.text(pageData.size);
    };

    const renderCardSummary = (summaryData) => {
        $activeCount.text(summaryData.activeStudents ?? 0);
        $onboardingCount.text(summaryData.onboardingStudents ?? 0);
        $openTicketCount.text(summaryData.openSupportTickets ?? 0);
        $openTicketBadge.text(summaryData.openSupportTickets ?? 0);
    };

    const showError = (message) => {
        $apiError.text(message).removeClass('d-none');
    };

    const hideError = () => {
        $apiError.addClass('d-none').text('');
    };

    const loadStudents = () => {
        hideError();
        $tableBody.html(`
            <tr>
                <td colspan="6" class="text-center text-muted py-4">Đang tải dữ liệu...</td>
            </tr>
        `);

        $.ajax({
            url: '/api/admin/students',
            type: 'GET',
            dataType: 'json',
            data: {
                keyword: state.keyword || null,
                status: state.status || null,
                page: state.page,
                size: state.size
            },
            success: function (response) {
                const pageData = response && response.data ? response.data : null;
                if (!pageData) {
                    showError('Không nhận được dữ liệu học viên hợp lệ từ server.');
                    renderRows([]);
                    renderPagination({ totalPages: 0 });
                    renderSummary({
                        totalElements: 0,
                        page: 0,
                        size: state.size,
                        totalPages: 0
                    });
                    return;
                }

                renderRows(pageData.content || []);
                renderPagination(pageData);
                renderSummary(pageData);
            },
            error: function (xhr) {
                const message = xhr.responseText || 'Không thể tải danh sách học viên.';
                showError(message);
                renderRows([]);
                renderPagination({ totalPages: 0 });
                renderSummary({
                    totalElements: 0,
                    page: 0,
                    size: state.size,
                    totalPages: 0
                });
            }
        });
    };

    const loadStudentSummary = () => {
        $.ajax({
            url: '/api/admin/students/summary',
            type: 'GET',
            dataType: 'json',
            success: function (response) {
                const summaryData = response && response.data ? response.data : null;
                if (!summaryData) {
                    renderCardSummary({
                        activeStudents: 0,
                        onboardingStudents: 0,
                        openSupportTickets: 0
                    });
                    return;
                }

                renderCardSummary(summaryData);
            },
            error: function () {
                renderCardSummary({
                    activeStudents: 0,
                    onboardingStudents: 0,
                    openSupportTickets: 0
                });
            }
        });
    };

    $statusPills.on('click', function () {
        const $pill = $(this);
        $statusPills.removeClass('active');
        $pill.addClass('active');
        state.status = $pill.data('status') || '';
        state.page = 0;
        loadStudents();
    });

    $searchButton.on('click', function () {
        state.keyword = $keyword.val().trim();
        state.page = 0;
        state.size = Number($pageSize.val()) || 10;
        loadStudents();
    });

    $resetButton.on('click', function () {
        $keyword.val('');
        $pageSize.val('10');
        state.status = '';
        state.keyword = '';
        state.page = 0;
        state.size = 10;
        $statusPills.removeClass('active');
        $statusPills.filter('[data-status=""]').addClass('active');
        loadStudents();
    });

    $keyword.on('keydown', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            $searchButton.trigger('click');
        }
    });

    $pageSize.on('change', function () {
        state.size = Number($(this).val()) || 10;
        state.page = 0;
        loadStudents();
    });

    $pagination.on('click', '.page-link', function () {
        const $button = $(this);
        const $pageItem = $button.closest('.page-item');
        if ($pageItem.hasClass('disabled') || $pageItem.hasClass('active')) {
            return;
        }

        const nextPage = Number($button.data('page'));
        if (Number.isNaN(nextPage) || nextPage < 0) {
            return;
        }

        state.page = nextPage;
        loadStudents();
    });

    loadStudentSummary();
    loadStudents();
});
