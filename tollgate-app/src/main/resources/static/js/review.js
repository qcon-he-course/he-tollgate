// CR-124 accept and return answers on the review page without reloading
$(document).ready(function () {
    $('.review-form').on('submit', function (e) {
        e.preventDefault();
        var form = $(this);
        var row = form.closest('tr');
        $.post(form.data('review-url'), {
            action: form.data('action'),
            note: form.find('input[name=note]').val()
        }, function (data) {
            if (!data.ok) {
                row.find('.review-error').text(data.message).show();
                return;
            }
            row.find('.review-status').text(data.reviewStatus);
            row.find('.review-buttons').hide();
            if (data.state === 'AWAITING_HAULIER') {
                $('#case-state').text('Answer returned. The case is waiting for the haulier.').show();
            } else if (data.state === 'IN_REVIEW') {
                $('#case-state').text('All required answers accepted. The case has been assessed.').show();
            } else if (data.state === 'LODGED') {
                $('#case-state').text('Answer accepted.').show();
            }
        });
    });
});
