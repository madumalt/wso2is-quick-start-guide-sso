<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  %>

<!DOCTYPE html>
<html >
<head>
    <meta charset="UTF-8">
    <title>Login form using HTML5 and CSS3</title>
    <link rel="stylesheet" href="css/loginStylesheet.css">
</head>

<body>
    <body>
        <div class="container">
            <section id="content">
                <form action="samlsso" method="post">
                    <h1>Login Form</h1>
                    <div>
                        <input type="text" placeholder="Username" required="" name="username" id="username" />
                    </div>
                    <div>
                        <input type="password" placeholder="Password" required="" name="password" id="password" />
                    </div>
                    <div>
                        <input type="submit" value="Log in" />
                        <a href="#">Lost your password?</a>
                        <a href="#">Register</a>
                    </div>
                </form><!-- form -->
                <!-- logged in user showing -->
                <div class="button">
                    <a href="#"><%=request.getAttribute("authorization")%></a>
                </div><!-- button -->
            </section><!-- content -->
        </div><!-- container -->
    </body>
    <script src="js/loginIndex.js"></script>
</body>
</html>
