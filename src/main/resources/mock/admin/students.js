$(function () {
    const $modal = $('#studentProfileModal');
    if (!$modal.length) {
        return;
    }

    const profileModal = new bootstrap.Modal($modal[0]);
    const studentRowSelector = '.student-row';
    const getStudentRows = () => $(studentRowSelector);

    const ui = {
        name: $('#modalStudentName'),
        meta: $('#modalStudentMeta'),
        email: $('#modalStudentEmail'),
        join: $('#modalJoinDate'),
        mentorLabel: $('#modalMentorLabel'),
        statusBadge: $('#modalStatusBadge'),
        initials: $('#modalStudentInitials'),
        progressLabel: $('#modalProgressLabel'),
        progressBar: $('#modalProgressBar'),
        goals: $('#modalGoals'),
        timeline: $('#modalTimeline'),
        noteContent: $('#modalNoteContent'),
        noteMeta: $('#modalNoteMeta'),
        courseInfo: $('#modalCourseInfo'),
        cohortInfo: $('#modalCohortInfo'),
        riskLabel: $('#modalRiskLabel'),
        alertName: $('#modalAlertName'),
        statusSelect: $('#modalStatusSelect'),
        mentorSelect: $('#modalMentorSelect'),
        cohortInput: $('#modalCohortInput'),
        noteInput: $('#modalNoteInput'),
        followUpDate: $('#modalFollowUpDate')
    };

    const fillModal = (data) => {
        const name = data.name || '';
        const course = data.course || '';
        const cohort = data.cohort || '';
        const progress = data.progress || 0;
        const moduleLabel = data.module || '';

        ui.name.text(name);
        ui.meta.text(`${course} · ${cohort}`);
        ui.email.text(data.email || '');
        ui.join.text(data.join || '');
        ui.mentorLabel.text(data.mentor || '');
        ui.courseInfo.text(course);
        ui.cohortInfo.text(cohort);
        ui.riskLabel.text(data.risk || 'Chưa xác định');
        ui.statusBadge.text(data.status || '').attr('class', `badge ${data.statusClass || 'bg-secondary'}`);
        ui.alertName.text(name);

        const initials = name
            .split(' ')
            .filter(Boolean)
            .map((chunk) => chunk.charAt(0))
            .join('')
            .slice(0, 2)
            .toUpperCase();
        ui.initials.text(initials || '');

        ui.progressLabel.text(`${progress}% · ${moduleLabel}`);
        ui.progressBar
            .css('width', `${progress}%`)
            .attr('class', `progress-bar ${data.progressClass || 'bg-primary'}`);

        ui.goals.empty();
        if (data.goals) {
            data.goals.split('|').forEach((goal) => {
                const value = goal.trim();
                if (!value) {
                    return;
                }
                $('<span>').addClass('goal-pill').text(value).appendTo(ui.goals);
            });
        } else {
            $('<small>').addClass('text-muted').text('Chưa có mục tiêu nào').appendTo(ui.goals);
        }

        ui.timeline.empty();
        if (data.timeline) {
            data.timeline.split('|').forEach((event) => {
                $('<li>').html(`<span class="timeline-dot"></span>${event.trim()}`).appendTo(ui.timeline);
            });
        } else {
            $('<li>').addClass('text-muted').text('Chưa có hoạt động nào').appendTo(ui.timeline);
        }

        ui.noteContent.text(data.note || 'Chưa có ghi chú');
        ui.noteMeta.text(data.noteUpdated || '—');

        if (ui.statusSelect.length) {
            const hasStatusOption = ui.statusSelect.find('option').filter((_, option) => $(option).val() === data.status)
                .length;
            ui.statusSelect.val(hasStatusOption ? data.status : '');
        }

        if (ui.mentorSelect.length) {
            const hasMentorOption = ui.mentorSelect.find('option').filter((_, option) => $(option).val() === data.mentor)
                .length;
            ui.mentorSelect.val(hasMentorOption ? data.mentor : 'Khác');
        }

        ui.cohortInput.val(cohort);
        ui.noteInput.val(data.note || '');
        ui.followUpDate.val(data.followup || '');

        profileModal.show();
    };

    $(document).on('click', studentRowSelector, function (event) {
        if ($(event.target).closest('button').length) {
            return;
        }
        fillModal($(this).data());
    });

    const $demoTrigger = $('#modalDemoTrigger');
    if ($demoTrigger.length) {
        $demoTrigger.on('click', () => {
            const $target = $('.student-row[data-student-id="mai-le"]').first();
            const $row = $target.length ? $target : getStudentRows().first();
            if ($row.length) {
                fillModal($row.data());
            }
        });
    }

    const $quickForm = $('#studentQuickUpdateForm');
    if ($quickForm.length) {
        $quickForm.on('submit', (event) => {
            event.preventDefault();
            const $alert = $('#modalUpdateAlert');
            if ($alert.length) {
                $alert.removeClass('d-none');
                setTimeout(() => $alert.addClass('d-none'), 2600);
            }
        });
    }

    const $addStudentModal = $('#addStudentModal');
    const addStudentModalInstance = $addStudentModal.length ? new bootstrap.Modal($addStudentModal[0]) : null;
    const $addStudentForm = $('#addStudentForm');
    const $studentTableBody = $('.table.align-middle tbody').first();

    const slugify = (text) => text
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '');

    const escapeAttr = (value = '') => String(value)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');

    const escapeHtml = (value = '') => String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');

    if ($addStudentForm.length) {
        $addStudentForm.on('submit', (event) => {
            event.preventDefault();

            const name = $('#newStudentName').val().trim();
            const email = $('#newStudentEmail').val().trim();
            const course = $('#newStudentCourse').val().trim();
            const cohort = $('#newStudentCohort').val().trim();
            const startDate = $('#newStudentStart').val();
            const mentor = $('#newStudentMentor').val();
            const goalsInput = $('#newStudentGoal').val().trim();
            const note = $('#newStudentNote').val().trim();

            if (!name || !email || !course || !cohort || !startDate) {
                return;
            }

            const goalDataset = goalsInput
                ? goalsInput.split(',').map((item) => item.trim()).filter(Boolean).join('|')
                : '';
            const slug = slugify(`${name}-${Date.now()}`) || `student-${Date.now()}`;

            if ($studentTableBody.length) {
                const safeName = escapeAttr(name);
                const safeEmail = escapeAttr(email);
                const safeCourse = escapeAttr(course);
                const safeCohort = escapeAttr(cohort);
                const safeMentor = escapeAttr(mentor);
                const safeGoals = escapeAttr(goalDataset);
                const safeTimeline = escapeAttr(`Vừa tạo tài khoản · ${startDate}`);
                const safeNote = escapeAttr(note);
                const safeJoin = escapeAttr(startDate);
                const safeFollow = escapeAttr(startDate);
                const noteStamp = escapeAttr(`System · ${new Date().toLocaleDateString('vi-VN')}`);

                const rowHtml = `
                        <tr class="student-row" data-student-id="${slug}" 
                                            data-name="${safeName}" 
                                            data-email="${safeEmail}" 
                                            data-course="${safeCourse}" 
                                            data-cohort="${safeCohort}" 
                                            data-progress="0" 
                                            data-module="Module 0/1" 
                                            data-status="Onboarding" 
                                            data-status-class="bg-info text-dark" 
                                            data-join="${safeJoin}" 
                                            data-mentor="${safeMentor}" 
                                            data-goals="${safeGoals}" 
                                            data-timeline="${safeTimeline}" 
                                            data-note="${safeNote}" 
                                            data-note-updated="${noteStamp}" 
                                            data-risk="Thấp" 
                                            data-progress-class="bg-info" 
                                            data-followup="${safeFollow}">
                        <td>
                            <strong>${escapeHtml(name)}</strong>
                            <div class="text-muted small">${escapeHtml(email)}</div>
                        </td>
                        <td>${escapeHtml(course)}</td>
                        <td>${escapeHtml(cohort)}</td>
                        <td style="min-width:140px;">
                            <div class="d-flex justify-content-between small">
                                <span>0%</span>
                                <span>Module 0/1</span>
                            </div>
                            <div class="progress progress-mini bg-light">
                                <div class="progress-bar bg-info" style="width:0%;"></div>
                            </div>
                        </td>
                        <td><span class="badge bg-info text-dark">Onboarding</span></td>
                        <td class="text-end">
                            <button type="button" class="btn btn-sm btn-outline-secondary me-2">Gửi tài liệu</button>
                            <button type="button" class="btn btn-sm btn-outline-primary">Nhắc kích hoạt</button>
                        </td>
                    </tr>`;
                $studentTableBody.prepend(rowHtml);
            }

            const $alert = $('#addStudentAlert');
            if ($alert.length) {
                $alert.removeClass('d-none');
                setTimeout(() => $alert.addClass('d-none'), 3000);
            }

            $addStudentForm[0].reset();

            if (addStudentModalInstance) {
                setTimeout(() => addStudentModalInstance.hide(), 600);
            }
        });
    }
});
