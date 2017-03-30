<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="index.css">
<title>Search Engine</title>
<style>
        input[type=text] {
        	font-size: 18px;
            box-sizing: border-box;
            width: 260px;
            background-color: white;
            padding: 12px 20px 12px 40px;
            -webkit-transition: width 0.4s ease-in-out;
            transition: width 0.4s ease-in-out;
        }

        input[type=text]:focus {
            width: 60%;
        }
    </style>
</head>
<body>
	<div>
		<img
			src="https://s-media-cache-ak0.pinimg.com/originals/db/cc/22/dbcc22aea578bc97391aca6b3d300591.jpg"
			alt="Google" />

		<form action="results" method="post" autocomplete="on">

			<input type="text" name="search" class="search" autocomplete="on" placeholder="Enter your search term"><br> 
			<input type="submit" value="search" class="button">
				
		</form>
	</div>
</body>
</html>
