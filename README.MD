# ICS Search Engine
---------
[![][travis img]][travis]
[![][coverage img]][coverage]
[![][sonar img]][sonar]
[![][lines img]][lines]

[travis]:https://travis-ci.org/vivekpn/ICS_Search_Engine/builds
[travis img]:https://api.travis-ci.org/vivekpn/ICS_Search_Engine.png
[coverage]:http://codecov.io/github/vivekpn/ICS_Search_Engine?branch=master
[coverage img]:http://codecov.io/github/vivekpn/ICS_Search_Engine/coverage.svg?branch=master
[sonar]:https://sonarqube.com/dashboard?id=com.project%3Aics-search-engine
[sonar img]:https://sonarqube.com/api/badges/measure?key=com.project:ics-search-engine&metric=sqale_debt_ratio
[lines]:https://sonarqube.com/component_measures/metric/ncloc/list?id=com.project%3Aics-search-engine
[lines img]:https://sonarqube.com/api/badges/measure?key=com.project:ics-search-engine&metric=ncloc

-------------
The search engine is implemented in Java. This repository contains the complete implementation of search engine other than the crawler. The crawled data is present in the data subfolder. The search engine consists of the following.
1) A tokenizer where the liguistic models are applied to the content of the HTML page forming the tokens. Stopwords are removed and the words are stemmed using snowball stemmer.
2) Other properties of a webpage such as anchor links, anchor texts, HTML tags (Eg: title, b, i, h1, h2, p, ...) are extracted from the page and stored in the database.
3) MongoDB is used for storing the inverted index.
