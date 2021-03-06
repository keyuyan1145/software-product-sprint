// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*; 


@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    Entity commentEntity = new Entity("comment");

    String comment = request.getParameter("comment");
    String name = request.getParameter("name");
    Date date = new Date();

    commentEntity.setProperty("date", date);
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("comment", comment);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    
    response.sendRedirect("/index.html");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Query query = new Query("comment").addSort("date", SortDirection.DESCENDING);
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery result = datastore.prepare(query);

    List<Comment> commentList = new ArrayList<>();

    String languageCode = request.getHeader("languageCode");
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    for (Entity entity : result.asIterable()) {
      Date date = (Date) entity.getProperty("date");

      Translation transName =
        translate.translate((String) entity.getProperty("name"), Translate.TranslateOption.targetLanguage(languageCode));
      String name = transName.getTranslatedText();
      
      Translation transComment =
        translate.translate((String) entity.getProperty("comment"), Translate.TranslateOption.targetLanguage(languageCode));
      String comment = transComment.getTranslatedText();

      Comment commentObj = new Comment(name, comment, date);
      commentList.add(commentObj);
    }

    Gson gson = new Gson();

    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(gson.toJson(commentList));
  }

}
