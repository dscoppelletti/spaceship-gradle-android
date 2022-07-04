<!DOCTYPE html>
<html>
<head>
    <title>Credits</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
    <h1>Credits</h1>
    <table>
        <tr>
            <th>Component</th>
            <th>Owner</th>
            <th>License</th>
        </tr>
        <#list credits as credit>
            <tr>
                <td>${credit.component}</td>
                <td>${credit.owner.text}</td>
                <td>${credit.license.text}</td>
            </tr>
        </#list>
    </table>
</body>
</html>