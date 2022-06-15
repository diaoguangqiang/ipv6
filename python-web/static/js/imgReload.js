function imgreload() {
	$.ajax({
		//url: "/imgReload",
		timeout: 10000, //超时时间设置为10秒；
		success: function(data) {
			$("#imgreload").html(data)
		},
		error: function(xhr, type, errorThrown) {

		}
	});
}