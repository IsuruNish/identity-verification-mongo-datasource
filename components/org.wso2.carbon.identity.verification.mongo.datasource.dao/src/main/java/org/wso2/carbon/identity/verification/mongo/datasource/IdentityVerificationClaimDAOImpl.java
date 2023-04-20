/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.verification.mongo.datasource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.mongodb.MongoClientException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.model.Filters;

import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationServerException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import com.google.gson.Gson;
import org.bson.Document;
import org.bson.conversions.Bson;

import static  org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.ERROR_PROCESSING_IDV_CLAIM;
import static  org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.ERROR_ADDING_IDV_CLAIMS;
import static  org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.ERROR_UPDATING_IDV_CLAIM;
import static  org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.ERROR_RETRIEVING_IDV_CLAIM;
import static  org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.ERROR_RETRIEVING_IDV_CLAIMS;
import static  org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.ERROR_CHECKING_IDV_CLAIM_EXISTENCE;
import static  org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.ERROR_CHECKING_IDV_CLAIM_DATA_EXISTENCE;
import static  org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.ERROR_DELETING_IDV_CLAIM;

/**
 * Identity verification claim DAO class.
 */
public class IdentityVerificationClaimDAOImpl implements IdentityVerificationClaimDAO {

    /**
     * Get priority value for the {@link IdentityVerificationClaimDAO}.
     *
     * @return Priority value for the DAO.
     */
    @Override
    public int getPriority() {

        return 1;
    }

    /**
     * Add the identity verification claim.
     *
     * @param idvClaimList IdentityVerificationClaim list.
     * @param tenantId Tenant id.
     * @throws IdentityVerificationException Identity verification claim management exception.
     */
    @Override
    public void addIdVClaimList(List<IdVClaim> idvClaimList, int tenantId) throws IdentityVerificationException {

        String url = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_URL_REGEX);
        String db = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_NAME_REGEX);
        String collectionName = IdentityUtil.
                getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_COLLECTION_REGEX);
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoDatabase dbObj = mongoClient.getDatabase(db);
            Gson gson = new Gson();
            ObjectMapper mapper = new ObjectMapper();
            List<Document> documents = new ArrayList<>();
            for (IdVClaim idVClaim : idvClaimList) {
                String jsonStr = null;
                jsonStr = gson.toJson(idVClaim);
                ObjectNode jsonNode = null;
                jsonNode = (ObjectNode) mapper.readTree(jsonStr);
                jsonNode.put("tenantId", tenantId);
                String json = null;
                json = mapper.writeValueAsString((jsonNode));
                Document doc = Document.parse(json);
                documents.add(doc);
            }
            MongoCollection<Document> collection = dbObj.getCollection(collectionName);
            collection.insertMany(documents);
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_ADDING_IDV_CLAIMS.getCode(), ERROR_ADDING_IDV_CLAIMS.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new IdentityVerificationServerException(ERROR_PROCESSING_IDV_CLAIM.getCode(), ERROR_PROCESSING_IDV_CLAIM.getMessage(), e);
        }
    }

    /**
     * Update the identity verification claim by the user id.
     *
     * @param idVClaim IdentityVerificationClaim.
     * @param tenantId Tenant id.
     * @throws IdentityVerificationException Identity verification claim management exception.
     */
    @Override
    public void updateIdVClaim(IdVClaim idVClaim, int tenantId) throws IdentityVerificationException {

        //"UPDATE IDV_CLAIM SET IS_VERIFIED = ?, METADATA = ? WHERE USER_ID = ? AND UUID = ? AND TENANT_ID = ?";
        //idVClaimId = claimId = uuid ?
//        "SELECT UUID, USER_ID, CLAIM_URI, TENANT_ID, IDVP_ID, IS_VERIFIED, METADATA FROM IDV_CLAIM WHERE USER_ID = ? AND UUID = ? AND TENANT_ID = ?"

        String url = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_URL_REGEX);
        String db = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_NAME_REGEX);
        String collectionName = IdentityUtil.
                getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_COLLECTION_REGEX);
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoDatabase dbObj = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = dbObj.getCollection(collectionName);
            Bson filter = Filters.and(
                    Filters.eq("uuid", idVClaim.getUuid()),
                    Filters.eq("tenantId", tenantId));
            Document metadataDoc = Document.parse(idVClaim.getMetadata().toString());
            Bson update = Updates.combine(
                    Updates.set("status", idVClaim.getStatus()),
                    Updates.set("metadata", metadataDoc),
                    Updates.set("uuid", idVClaim.getUuid()),
                    Updates.set("userId", idVClaim.getUserId()));
            UpdateResult result = collection.updateOne(filter , update);
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_UPDATING_IDV_CLAIM.getCode(), ERROR_UPDATING_IDV_CLAIM.getMessage(), e);
        }
    }

    /**
     * Get the identity verification claim.
     *
     * @param userId User id.
     * @param idVClaimId Identity verification claim id.
     * @param tenantId Tenant id.
     * @return Identity verification claim.
     * @throws IdentityVerificationException Identity verification claim management exception.
     */
    @Override
    public IdVClaim getIDVClaim(String userId, String idVClaimId, int tenantId) throws IdentityVerificationException {

        String url = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_URL_REGEX);
        String db = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_NAME_REGEX);
        String collectionName = IdentityUtil.
                getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_COLLECTION_REGEX);
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoDatabase dbObj = mongoClient.getDatabase(db);
            ObjectMapper mapper = new ObjectMapper();
            MongoCollection<Document> collection = dbObj.getCollection(collectionName);
            Document doc = collection.find(Filters.and(
                            Filters.eq("uuid", idVClaimId),
                            Filters.eq("tenantId", tenantId))).first();
                if (doc != null){
                    IdVClaim idVClaim = null;
                    Gson gson = new Gson();
                    String json = doc.toJson();
                    JsonNode rootNode = mapper.readTree(json);
                    JsonNode metadataJsonNode = rootNode.get("metadata");
                    idVClaim = gson.fromJson(json, IdVClaim.class);
                    if (metadataJsonNode != null){
                        String metadataJson = metadataJsonNode.toString();
                        JSONObject jsonObj = new JSONObject(metadataJson);
                        idVClaim.setMetadata(jsonObj);
                    }
                    return idVClaim;
                }
                return null;
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_RETRIEVING_IDV_CLAIM.getCode(), ERROR_RETRIEVING_IDV_CLAIM.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new IdentityVerificationServerException(ERROR_PROCESSING_IDV_CLAIM.getCode(), ERROR_PROCESSING_IDV_CLAIM.getMessage(), e);
        }
    }

    /**
     * Get the identity verification claims.
     *
     * @param tenantId Tenant id.
     * @param userId   User id.
     * @return Identity verification claim.
     * @throws IdentityVerificationException Identity verification claim management exception.
     */
    @Override
    public IdVClaim[] getIDVClaims(String userId, int tenantId) throws IdentityVerificationException {

        String url = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_URL_REGEX);
        String db = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_NAME_REGEX);
        String collectionName = IdentityUtil.
                getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_COLLECTION_REGEX);
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoDatabase dbObj = mongoClient.getDatabase(db);
            ObjectMapper mapper = new ObjectMapper();
            MongoCollection<Document> collection = dbObj.getCollection(collectionName);
            Bson filter = Filters.and(Filters.eq("userId", userId), Filters.eq("tenantId", tenantId));
            MongoCursor<Document> documents = collection.find(filter).iterator();
            List<IdVClaim> idVClaims = new ArrayList<>();
            while (documents.hasNext()) {
                IdVClaim idVClaim = null;
                Gson gson = new Gson();
                String json = documents.next().toJson();
                JsonNode metadataJsonNode = mapper.readTree(json).get("metadata");
                idVClaim = gson.fromJson(json, IdVClaim.class);
                if (metadataJsonNode != null){
                    idVClaim.setMetadata(new JSONObject(metadataJsonNode.toString()));
                }
                idVClaims.add(idVClaim);
            }
            return idVClaims.toArray(new IdVClaim[0]);
        } catch (JsonProcessingException e) {
            throw new IdentityVerificationServerException(ERROR_PROCESSING_IDV_CLAIM.getCode(), ERROR_PROCESSING_IDV_CLAIM.getMessage(), e);
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_RETRIEVING_IDV_CLAIMS.getCode(), ERROR_RETRIEVING_IDV_CLAIMS.getMessage(), e);
        }
    }

    /**
     * Delete the identity verification claim.
     *
     * @param userId     User id.
     * @param idVClaimId Identity verification claim id.
     * @param tenantId   Tenant id.
     * @throws IdentityVerificationException Identity verification exception.
     */
    @Override
    public void deleteIdVClaim(String userId, String idVClaimId, int tenantId) throws IdentityVerificationException {

        String url = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_URL_REGEX);
        String db = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_NAME_REGEX);
        String collectionName = IdentityUtil.
                getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_COLLECTION_REGEX);
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoDatabase dbObj = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = dbObj.getCollection(collectionName);
            Bson filter = Filters.and(
                    Filters.eq("userId", userId),
                    Filters.eq("uuid", idVClaimId),
                    Filters.eq("tenantId", tenantId)
                                         );
            collection.deleteOne(filter);
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_DELETING_IDV_CLAIM.getCode(), ERROR_DELETING_IDV_CLAIM.getMessage(), e);
        }
    }

    /**
     * Check whether the identity verification claim exist.
     *
     * @param userId        User id.
     * @param idVPId        Identity verification provider id.
     * @param uri           Claim uri.
     * @param tenantId      Tenant id.
     * @return True if the identity verification claim exist.
     * @throws IdentityVerificationException Identity verification exception.
     */
    @Override
    public boolean isIdVClaimDataExist(String userId, String idVPId, String uri, int tenantId)
            throws IdentityVerificationException {

        String url = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_URL_REGEX);
        String db = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_NAME_REGEX);
        String collectionName = IdentityUtil.
                getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_COLLECTION_REGEX);
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoCollection<Document> collection = mongoClient.getDatabase(db).getCollection(collectionName);
            Document doc = collection.find(Filters.and(
                            Filters.eq("userId", userId),
                            Filters.eq("tenantId", tenantId),
                            Filters.eq("idVPId", idVPId),
                            Filters.eq("claimUri", uri))).first();
            if (doc != null){
                return true;
            }
            return false;
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_CHECKING_IDV_CLAIM_DATA_EXISTENCE.getCode(), ERROR_CHECKING_IDV_CLAIM_DATA_EXISTENCE.getMessage(), e);
        }
    }

    /**
     * Check whether the identity verification claim exist.
     *
     * @param claimId  Identity verification claim id.
     * @param tenantId Tenant id.
     * @return True if the identity verification claim exist.
     * @throws IdentityVerificationException Identity verification exception.
     */
    @Override
    public boolean isIdVClaimExist(String claimId, int tenantId) throws IdentityVerificationException {

        String url = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_URL_REGEX);
        String db = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_NAME_REGEX);
        String collectionName = IdentityUtil.
                getProperty(IdentityVerificationConstants.DatabaseConstants.DATABASE_COLLECTION_REGEX);
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoCollection<Document> collection = mongoClient.getDatabase(db).getCollection(collectionName);
            Document doc = collection.find(Filters.and(
                    Filters.eq("uuid", claimId),
                    Filters.eq("tenantId", tenantId))).first();
            if (doc != null) {
                return true;
            }
            return false;
        }  catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_CHECKING_IDV_CLAIM_EXISTENCE.getCode(), ERROR_CHECKING_IDV_CLAIM_EXISTENCE.getMessage(), e);
        }
    }

}
