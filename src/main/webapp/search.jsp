<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page
	import="java.util.*, java.io.*, com.se.servlets.SearchResultsUI, com.se.data.Document"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="search.css">
<title>Search Engine</title>
</head>
<body>
	<form action="results" method="post" autocomplete="on" align="center">

		<input type="text" name="search" class="search" autocomplete="on"
			placeholder="Enter your search term">
			<input type="submit"
			value="search" class="button"><br>
		<br>

	</form>

	<table cellspacing="50">

		<%
			List<SearchResultsUI> searchResultsUIs = (List<SearchResultsUI>) request.getAttribute("searchResultsUIs");
		int page_search=1;
		if(request.getParameter("page")!=null){	
			out.println("Passed the method");
			page_search = Integer.parseInt(request.getParameter("page"));
		
			int total=2;
/* 			int page_no = Integer.parseInt(page_search); */
			int page_no=page_search;
			int init_page = ((page_no-1)*total)+1;
			
			for (int i = init_page; i < (page_no+total); i++) {
				//out.println(searchResults.get(i));
			}}
			//out.print("Results for search query: ");
			//out.print("\n");
			

			for (SearchResultsUI result : searchResultsUIs) {
		%>

		<tr>
			<td><a href="<%="//" + result.getUrl()%>" style="font-size: 18px;"> <%=result.getTitle()%></a><br>
			<div style="color:#006621; font-size: 14px;"> <%=result.getUrl()%> </div> 
			<div style="font-size:small";><%=result.getSnippet()%></div></td>
			<td>SCORE: <%=result.getScore().toString()%>
			</td>



		</tr>

		<%
			}
		%>
	</table>
	<%--  <div class="pagination">


<% 		
	int size=searchResultsUIs.size();
	for(int i=0;i<size;i++)
	{%>
		<a
			href="http://localhost:8080/web-indexer/search.jsp?page=<%out.print(i);%>"><%out.print(i);%></a>

		<%}%>
	</div>--%>
</body>
</html>