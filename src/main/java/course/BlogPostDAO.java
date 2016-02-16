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

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class BlogPostDAO {
    MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    public Document findByPermalink(String permalink) {
        Document post = postsCollection.find(new Document("permalink", permalink)).first();
        return post;
    }

    public List<Document> findByDateDescending(int limit) {
        List<Document> posts = new ArrayList<>();
        FindIterable<Document> cursor = postsCollection.find().sort(new Document("date", -1)).limit(limit);
        cursor.into(posts);
        return posts;
    }

    public List<Document> findByTagDateDescending(final String tag) {
        List<Document> posts = new ArrayList<>();
        Document query = new Document("tags", tag);
        System.out.println("/tag query: " + query.toString());
        FindIterable<Document> cursor = postsCollection.find(query).sort(new Document("date", -1)).limit(10);
        cursor.into(posts);
        return posts;
    }

    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        Document post = new Document("title", title);
        post.append("author", username);
        post.append("body", body);
        post.append("permalink", permalink);
        post.append("tags", tags);
        post.append("comments", new java.util.ArrayList());
        post.append("date", new java.util.Date());

        try {
            postsCollection.insertOne(post);
            System.out.println("Inserting blog post with permalink " + permalink);
        } catch (Exception e) {
            System.out.println("Error inserting post");
            return null;
        }

        return permalink;
    }

    public void addPostComment(final String name, final String email, final String body, final String permalink) {
        Document comment = new Document("author", name).append("body", body);
        if (email != null && !email.equals("")) {
            comment.append("email", email);
        }

        UpdateResult result = postsCollection.updateOne(new Document("permalink", permalink),
                                                        new Document("$push", new Document("comments", comment)));
    }

}
