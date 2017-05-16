package com.suhang.sample;

import java.util.List;

/**
 * Created by 苏杭 on 2017/2/3 15:45.
 */

public class GithubBean extends ErrorBean{
    public static final String METHOD = "getGithubData";

    private boolean error;

    private List<ResultsEntity> results;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public List<ResultsEntity> getResults() {
        return results;
    }

    public void setResults(List<ResultsEntity> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "GithubBean{" +
                "error=" + error +
                ", results=" + results +
                '}';
    }

    public static class ResultsEntity {
        @Override
        public String toString() {
            return "ResultsEntity{" +
                    "_id='" + _id + '\'' +
                    ", content='" + content + '\'' +
                    ", created_at='" + created_at + '\'' +
                    ", publishedAt='" + publishedAt + '\'' +
                    ", rand_id='" + rand_id + '\'' +
                    ", title='" + title + '\'' +
                    ", updated_at='" + updated_at + '\'' +
                    '}';
        }

        private String _id;
        private String content;
        private String created_at;
        private String publishedAt;
        private String rand_id;
        private String title;
        private String updated_at;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getPublishedAt() {
            return publishedAt;
        }

        public void setPublishedAt(String publishedAt) {
            this.publishedAt = publishedAt;
        }

        public String getRand_id() {
            return rand_id;
        }

        public void setRand_id(String rand_id) {
            this.rand_id = rand_id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }
    }
}
