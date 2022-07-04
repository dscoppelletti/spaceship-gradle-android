<!DOCTYPE html>
<html>
<head>
    <title>Credits</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
    <#list credits as credit>
		<p>
			<table>
				<tr>
					<th align="left">Component:</th>
					<td>${credit.component}</td>
				</tr>
				<tr>
					<th align="left">Owner:</th>
					<td>${credit.owner.text}</td>
				</tr>
				<tr>
					<th align="left">License:</th>
					<td>${credit.license.text}</td>
				</tr>
			</table>
		</p>
	</#list>
</body>
</html>