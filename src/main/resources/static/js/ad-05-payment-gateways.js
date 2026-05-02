$(function () {
    const $alert = $('#paymentGatewayAlert');
    const $gatewayList = $('#gatewayList');
    const $routingRuleList = $('#routingRuleList');
    const $healthEventList = $('#healthEventList');
    const addGatewayModalElement = document.getElementById('addGatewayModal');
    const addGatewayModal = addGatewayModalElement ? new bootstrap.Modal(addGatewayModalElement) : null;

    let selectedGatewayCode = null;

    // Escape HTML truoc khi render du lieu API ra DOM.
    const escapeHtml = (value = '') => $('<div>').text(value == null ? '' : value).html();

    // Hien thi alert chung cho page gateway.
    const showAlert = (message, type = 'success') => {
        $alert.removeClass('d-none alert-success alert-danger alert-warning alert-info')
            .addClass(`alert-${type}`)
            .text(message);
    };

    // An alert chung khi bat dau thao tac moi.
    const hideAlert = () => {
        $alert.addClass('d-none').text('');
    };

    // Lay payload gateway tu form detail.
    const collectGatewayPayload = () => ({
        displayName: $('#displayName').val().trim(),
        providerType: $('#providerType').val().trim(),
        description: $('#description').val().trim(),
        merchantId: $('#merchantId').val().trim(),
        partnerCode: $('#partnerCode').val().trim(),
        secretKey: $('#secretKey').val().trim(),
        paymentEndpoint: $('#paymentEndpoint').val().trim(),
        returnUrl: $('#returnUrl').val().trim(),
        webhookUrl: $('#webhookUrl').val().trim(),
        ipAllowlist: $('#ipAllowlist').val().trim(),
        enabled: $('#enabled').is(':checked'),
        sandboxMode: $('#sandboxMode').is(':checked'),
        routingPriority: Number($('#routingPriority').val()) || 99,
        transactionFeePercent: Number($('#transactionFeePercent').val()) || 0,
        status: $('#status').val()
    });

    // Tao payload gateway moi tu modal.
    const collectNewGatewayPayload = () => ({
        code: $('#newGatewayCode').val().trim(),
        displayName: $('#newDisplayName').val().trim(),
        providerType: $('#newProviderType').val(),
        merchantId: $('#newMerchantId').val().trim(),
        secretKey: $('#newSecretKey').val().trim(),
        webhookUrl: $('#newWebhookUrl').val().trim(),
        routingPriority: Number($('#newRoutingPriority').val()) || 10,
        enabled: true,
        sandboxMode: true,
        status: 'REVIEW',
        transactionFeePercent: 0,
        successRatePercent: 0
    });

    // Render cac card summary lay tu API.
    const renderSummary = (summary, gateways) => {
        $('#enabledGatewayCount').text(summary.enabledGateways ?? 0);
        $('#enabledGatewayLabel').text((gateways || [])
            .filter((gateway) => gateway.enabled)
            .map((gateway) => gateway.displayName)
            .join(', ') || 'Chua co gateway dang bat');
        $('#averageSuccessRate').text(summary.averageSuccessRatePercent ?? 0);
        $('#webhookErrorCount').text(summary.webhookErrors ?? 0);
        $('#environmentLabel').text(summary.environmentLabel || '--');
    };

    // Render danh sach gateway ben trai.
    const renderGatewayList = (gateways) => {
        if (!gateways || !gateways.length) {
            $gatewayList.html('<div class="text-center text-muted py-4">Chua co gateway nao.</div>');
            return;
        }

        const html = gateways.map((gateway) => {
            const active = gateway.code === selectedGatewayCode ? ' active' : '';
            const initials = (gateway.displayName || gateway.code || 'GW').slice(0, 2).toUpperCase();
            return `
                <div class="gateway-item${active}" data-gateway-code="${escapeHtml(gateway.code)}">
                    <div class="d-flex justify-content-between align-items-start gap-3">
                        <div class="d-flex gap-3">
                            <div class="gateway-logo bg-primary text-white">${escapeHtml(initials)}</div>
                            <div>
                                <strong>${escapeHtml(gateway.displayName)}</strong>
                                <small class="d-block text-muted">${escapeHtml(gateway.description || gateway.providerType)}</small>
                            </div>
                        </div>
                        <span class="${escapeHtml(gateway.statusBadgeClass || 'badge bg-secondary')}">${escapeHtml(gateway.status)}</span>
                    </div>
                    <div class="gateway-meta">
                        <span>Priority ${escapeHtml(gateway.routingPriority)}</span>
                        <span>Fee ${escapeHtml(gateway.transactionFeePercent)}%</span>
                        <span>${escapeHtml(gateway.successRatePercent)}%</span>
                    </div>
                </div>
            `;
        }).join('');

        $gatewayList.html(html);
    };

    // Render rule va health event lay tu API.
    const renderSupportPanels = (routingRules, healthEvents) => {
        $routingRuleList.html((routingRules || []).map((rule) => `
            <li class="list-group-item">${escapeHtml(rule)}</li>
        `).join('') || '<li class="list-group-item text-muted">Chua co routing rule.</li>');

        $healthEventList.html((healthEvents || []).map((event) => `
            <p class="mb-3"><span class="dot"></span>${escapeHtml(event)}</p>
        `).join('') || '<p class="mb-0 text-muted"><span class="dot"></span>Chua co health event.</p>');
    };

    // Do du lieu gateway dang chon vao form detail.
    const fillGatewayForm = (gateway) => {
        selectedGatewayCode = gateway.code;
        $('#gatewayCode').val(gateway.code);
        $('#gatewayTitle').text(`${gateway.displayName} gateway`);
        $('#gatewaySubtitle').text(gateway.description || gateway.providerType || 'Gateway configuration');
        $('#gatewayStatusBadge').attr('class', gateway.statusBadgeClass || 'badge bg-secondary').text(gateway.status || '--');
        $('#displayName').val(gateway.displayName || '');
        $('#providerType').val(gateway.providerType || '');
        $('#description').val(gateway.description || '');
        $('#merchantId').val(gateway.merchantId || '');
        $('#partnerCode').val(gateway.partnerCode || '');
        $('#secretKey').val('');
        $('#maskedSecretLabel').text(`Secret hien tai: ${gateway.maskedSecretKey || 'chua cau hinh'}`);
        $('#paymentEndpoint').val(gateway.paymentEndpoint || '');
        $('#returnUrl').val(gateway.returnUrl || '');
        $('#webhookUrl').val(gateway.webhookUrl || '');
        $('#ipAllowlist').val(gateway.ipAllowlist || '');
        $('#enabled').prop('checked', !!gateway.enabled);
        $('#sandboxMode').prop('checked', !!gateway.sandboxMode);
        $('#routingPriority').val(gateway.routingPriority ?? 99);
        $('#transactionFeePercent').val(gateway.transactionFeePercent ?? 0);
        $('#status').val(gateway.status || 'LIVE');
    };

    // Tai lai toan bo page data tu API.
    const loadPage = (preferredCode) => {
        hideAlert();
        $.ajax({
            url: '/api/admin/payment-gateways',
            type: 'GET',
            dataType: 'json',
            success: function (response) {
                const data = response && response.data ? response.data : {};
                const gateways = data.gateways || [];
                selectedGatewayCode = preferredCode || selectedGatewayCode || (gateways[0] ? gateways[0].code : null);
                renderSummary(data.summary || {}, gateways);
                renderGatewayList(gateways);
                renderSupportPanels(data.routingRules, data.healthEvents);

                const selectedGateway = gateways.find((gateway) => gateway.code === selectedGatewayCode);
                if (selectedGateway) {
                    fillGatewayForm(selectedGateway);
                    renderGatewayList(gateways);
                }
            },
            error: function (xhr) {
                showAlert(xhr.responseText || 'Khong the tai cau hinh cong thanh toan.', 'danger');
                $gatewayList.html('<div class="text-center text-danger py-4">Tai du lieu that bai.</div>');
            }
        });
    };

    // Luu gateway dang chon qua API.
    const saveSelectedGateway = () => {
        if (!selectedGatewayCode) {
            showAlert('Vui long chon gateway truoc khi luu.', 'warning');
            return;
        }

        $.ajax({
            url: `/api/admin/payment-gateways/${encodeURIComponent(selectedGatewayCode)}`,
            type: 'PUT',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(collectGatewayPayload()),
            success: function (response) {
                const gateway = response && response.data ? response.data : null;
                showAlert('Da luu cau hinh gateway.');
                loadPage(gateway ? gateway.code : selectedGatewayCode);
            },
            error: function (xhr) {
                showAlert(xhr.responseText || 'Khong the luu cau hinh gateway.', 'danger');
            }
        });
    };

    // Tao gateway moi qua API.
    const createGateway = () => {
        $.ajax({
            url: '/api/admin/payment-gateways',
            type: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(collectNewGatewayPayload()),
            success: function (response) {
                const gateway = response && response.data ? response.data : null;
                if (addGatewayModal) {
                    addGatewayModal.hide();
                }
                $('#addGatewayForm')[0].reset();
                showAlert('Da tao gateway moi.');
                loadPage(gateway ? gateway.code : null);
            },
            error: function (xhr) {
                showAlert(xhr.responseText || 'Khong the tao gateway moi.', 'danger');
            }
        });
    };

    // Test gateway dang chon qua API mock.
    const testSelectedGateway = () => {
        if (!selectedGatewayCode) {
            showAlert('Vui long chon gateway truoc khi test.', 'warning');
            return;
        }

        $.ajax({
            url: `/api/admin/payment-gateways/${encodeURIComponent(selectedGatewayCode)}/test`,
            type: 'POST',
            dataType: 'json',
            success: function (response) {
                const result = response && response.data ? response.data : {};
                showAlert(result.message || 'Da test gateway.', result.healthy ? 'success' : 'warning');
            },
            error: function (xhr) {
                showAlert(xhr.responseText || 'Khong the test gateway.', 'danger');
            }
        });
    };

    // Test tat ca gateway qua API mock.
    const testAllGateways = () => {
        $.ajax({
            url: '/api/admin/payment-gateways/test-all',
            type: 'POST',
            dataType: 'json',
            success: function (response) {
                const results = response && response.data ? response.data : [];
                const failed = results.filter((item) => !item.healthy).length;
                showAlert(failed
                    ? `Da test tat ca gateway, co ${failed} gateway can kiem tra.`
                    : 'Tat ca gateway dang healthy.',
                    failed ? 'warning' : 'success');
            },
            error: function (xhr) {
                showAlert(xhr.responseText || 'Khong the test tat ca gateway.', 'danger');
            }
        });
    };

    $gatewayList.on('click', '.gateway-item', function () {
        selectedGatewayCode = $(this).data('gateway-code');
        $.ajax({
            url: `/api/admin/payment-gateways/${encodeURIComponent(selectedGatewayCode)}`,
            type: 'GET',
            dataType: 'json',
            success: function (response) {
                const gateway = response && response.data ? response.data : null;
                if (gateway) {
                    fillGatewayForm(gateway);
                    $gatewayList.find('.gateway-item').removeClass('active');
                    $gatewayList.find(`[data-gateway-code="${selectedGatewayCode}"]`).addClass('active');
                }
            },
            error: function (xhr) {
                showAlert(xhr.responseText || 'Khong the tai chi tiet gateway.', 'danger');
            }
        });
    });

    $('#saveGatewayButton').on('click', saveSelectedGateway);
    $('#testGatewayButton').on('click', testSelectedGateway);
    $('#testAllGatewayButton').on('click', testAllGateways);
    $('#addGatewayForm').on('submit', function (event) {
        event.preventDefault();
        createGateway();
    });

    loadPage();
});
