// Call the dataTables jQuery plugin
$(document).ready(function() {
  // Nếu trang hiện tại (VD Dashboard) có set window.__dataTableOrder thì dùng nó để chỉ định
  // cột/chiều sort mặc định (VD sort theo "Ngày đặt" giảm dần thay vì cột đầu tiên như mặc định).
  // Các trang khác không set biến này thì giữ nguyên hành vi cũ (sort cột đầu, ascending).
  $('#dataTable').DataTable({
    order: window.__dataTableOrder || [[0, 'asc']]
  });
});