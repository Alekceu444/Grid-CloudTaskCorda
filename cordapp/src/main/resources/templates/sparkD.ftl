<html>

<head>
</head>

<body class="h-100">
<div class="my-4 mx-auto border-left">
                            <#list status as x >
                                <li class="list-group-item p-3">
                                    <form method='get'>
                                        <label class="text-muted d-block mb-2 text-center">${x}</label>
                                    </form>
                                </li>
                            </#list>
</body>
</html>