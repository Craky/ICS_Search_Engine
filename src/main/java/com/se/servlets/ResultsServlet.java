package com.se.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.se.data.SearchResult;
import com.se.query.QueryRunner;

public class ResultsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ResultsServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String query = request.getParameter("search");

		if (query == null) {
			query = "";
		}

		QueryRunner queryRunner = new QueryRunner();
		List<SearchResult> searchResults = queryRunner.search(query);
		List<SearchResultsUI> searchResultsUIs = new ArrayList<>();
		for (SearchResult searchResult : searchResults) {
			searchResultsUIs.add(new SearchResultsUI(searchResult));
		}

		request.setAttribute("searchResultsUIs", searchResultsUIs);
		request.setAttribute("query", query);
		request.getRequestDispatcher("search.jsp").forward(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
