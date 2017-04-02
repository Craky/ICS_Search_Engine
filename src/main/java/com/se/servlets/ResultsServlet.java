package com.se.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.se.data.SearchResult;
import com.se.index.AnchorTextProcessingMR;
import com.se.query.QueryRunner;

public class ResultsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AnchorTextProcessingMR.class);

	public ResultsServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {

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
		try {
			request.getRequestDispatcher("search.jsp").forward(request,
					response);
		} catch (ServletException | IOException exception) {
			LOGGER.error(exception.getMessage(), exception);
		}

	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {
		doGet(request, response);
	}

}
