package org.futto.app.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "futtoandroid-mobilehub-2138682397-Notification")

public class NotificationDO {
    private String _userId;
    private Double _creationDate;
    private String _content;
    private Boolean _isReaded;
    private String _noteId;
    private String _title;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBIndexHashKey(attributeName = "userId", globalSecondaryIndexName = "DateSorted")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBRangeKey(attributeName = "creationDate")
    @DynamoDBIndexRangeKey(attributeName = "creationDate", globalSecondaryIndexName = "DateSorted")
    public Double getCreationDate() {
        return _creationDate;
    }

    public void setCreationDate(final Double _creationDate) {
        this._creationDate = _creationDate;
    }
    @DynamoDBAttribute(attributeName = "content")
    public String getContent() {
        return _content;
    }

    public void setContent(final String _content) {
        this._content = _content;
    }
    @DynamoDBAttribute(attributeName = "isReaded")
    public Boolean getIsReaded() {
        return _isReaded;
    }

    public void setIsReaded(final Boolean _isReaded) {
        this._isReaded = _isReaded;
    }
    @DynamoDBAttribute(attributeName = "noteId")
    public String getNoteId() {
        return _noteId;
    }

    public void setNoteId(final String _noteId) {
        this._noteId = _noteId;
    }
    @DynamoDBAttribute(attributeName = "title")
    public String getTitle() {
        return _title;
    }

    public void setTitle(final String _title) {
        this._title = _title;
    }

}
