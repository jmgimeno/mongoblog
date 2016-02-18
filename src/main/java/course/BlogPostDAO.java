/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package course;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.push;

public class BlogPostDAO {
    MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    public Document findByPermalink(String permalink) {
        return postsCollection
                .find(eq("permalink", permalink))
                .first();
    }

    public List<Document> findByDateDescending(int limit) {
        return postsCollection
                .find()
                .sort(descending("date"))
                .limit(limit)
                .into(new ArrayList<>());
    }

    public List<Document> findByTagDateDescending(final String tag) {
        return postsCollection
                .find(eq("tags", tag))
                .sort(descending("date"))
                .limit(10)
                .into(new ArrayList<>());
    }

    public String addPost(String title, String body, List tags, String username) {
        try {
            String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
            permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
            permalink = permalink.toLowerCase();

            Document post = new Document("title", title)
                            .append("author", username)
                            .append("body", body)
                            .append("permalink", permalink)
                            .append("tags", tags)
                            .append("comments", new ArrayList<>())
                            .append("date", new Date());

            postsCollection.insertOne(post);
            return permalink;
        } catch (MongoException e) {
            return null;
        }
    }

    public void addPostComment(String name, String email, String body, String permalink) {
        Document comment = new Document("author", name)
                            .append("body", body);

        if (email != null && !email.equals("")) {
            comment.append("email", email);
        }

        postsCollection.updateOne(eq("permalink", permalink), push("comments", comment));
    }

}
