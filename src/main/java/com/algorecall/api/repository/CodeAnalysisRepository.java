package com.algorecall.api.repository;

import com.algorecall.api.model.CodeAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeAnalysisRepository extends MongoRepository<CodeAnalysis, String> {
    List<CodeAnalysis> findByUserIdOrderByCreatedAtDesc(String userId);
    
    Optional<CodeAnalysis> findByIdAndUserId(String id, String userId);
    
    List<CodeAnalysis> findByUserIdAndBookmarkedOrderByCreatedAtDesc(String userId, boolean bookmarked);
    
    List<CodeAnalysis> findTop5ByUserIdOrderByCreatedAtDesc(String userId);
    
    @Query("{ 'userId': ?0, $or: [ " +
           "{ 'title': { $regex: ?1, $options: 'i' } }, " +
           "{ 'language': { $regex: ?1, $options: 'i' } }, " +
           "{ 'category': { $regex: ?1, $options: 'i' } }, " +
           "{ 'mistakeCategory': { $regex: ?1, $options: 'i' } }, " +
           "{ 'sourceCode': { $regex: ?1, $options: 'i' } }, " +
           "{ 'overallSummary': { $regex: ?1, $options: 'i' } }, " +
           "{ 'userNotes': { $regex: ?1, $options: 'i' } } " +
           "] }")
    List<CodeAnalysis> search(String userId, String keyword);
}
