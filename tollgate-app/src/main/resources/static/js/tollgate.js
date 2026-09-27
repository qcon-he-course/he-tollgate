/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: tollgate.js 1071 2017-09-27 12:40:16Z koduya $
 */
var Tollgate = Tollgate || {};

/**
 * Filters the rows of a table to those containing the text typed in the filter box.
 * Usage: <input class="table-filter" data-table="#tableId"/>
 */
Tollgate.filterTable = function (input) {
    var text = $(input).val().toLowerCase();
    var table = $($(input).data('table'));
    table.find('tbody tr').each(function () {
        var row = $(this);
        if (text === '' || row.text().toLowerCase().indexOf(text) > -1) {
            row.show();
        } else {
            row.hide();
        }
    });
};

$(document).ready(function () {
    $('.table-filter').on('keyup change', function () {
        Tollgate.filterTable(this);
    });
});

// Revoke permit from the case page modal (TG-207)
$(document).ready(function () {
    $('#revoke-form').on('submit', function (e) {
        e.preventDefault();
        var form = $(this);
        var headers = {};
        headers[$('meta[name=_csrf_header]').attr('content')] = $('meta[name=_csrf]').attr('content');
        $.ajax({
            url: form.attr('action'),
            type: 'POST',
            data: form.serialize(),
            headers: headers,
            success: function (data) {
                if (data.ok) {
                    window.location.reload();
                } else {
                    $('#revoke-error').text(data.message).show();
                }
            },
            error: function () {
                $('#revoke-error').text('You are not allowed to revoke permits.').show();
            }
        });
    });
});
