package dev.langchain4j.store.embedding;

import dev.langchain4j.data.embedding.Embedding;

import java.util.List;

/**
 * Represents a store for embeddings, also known as a vector database.
 *
 * @param <Embedded> The class of the object that has been embedded. Typically, this is {@link dev.langchain4j.data.segment.TextSegment}.
 */
public interface EmbeddingStore<Embedded> {

    /**
     * Adds a given embedding to the store.
     *
     * @param embedding The embedding to be added to the store.
     * @return The auto-generated ID associated with the added embedding.
     */
    String add(Embedding embedding);

    /**
     * Adds a given embedding to the store.
     *
     * @param id        The unique identifier for the embedding to be added.
     * @param embedding The embedding to be added to the store.
     */
    void add(String id, Embedding embedding);

    /**
     * Adds a given embedding and the corresponding content that has been embedded to the store.
     *
     * @param embedding The embedding to be added to the store.
     * @param embedded  Original content that was embedded.
     * @return The auto-generated ID associated with the added embedding.
     */
    String add(Embedding embedding, Embedded embedded);

    /**
     * Adds multiple embeddings to the store.
     *
     * @param embeddings A list of embeddings to be added to the store.
     * @return A list of auto-generated IDs associated with the added embeddings.
     */
    List<String> addAll(List<Embedding> embeddings);

    /**
     * Adds multiple embeddings and their corresponding contents that have been embedded to the store.
     *
     * @param embeddings A list of embeddings to be added to the store.
     * @param embedded   A list of original contents that were embedded.
     * @return A list of auto-generated IDs associated with the added embeddings.
     */
    List<String> addAll(List<Embedding> embeddings, List<Embedded> embedded);

    /**
     * Finds the most relevant (closest in space) embeddings to the provided reference embedding.
     * By default, minScore is set to 0, which means that the results may include embeddings with low relevance.
     *
     * @param referenceEmbedding The embedding used as a reference. Returned embeddings should be relevant (closest) to this one.
     * @param maxResults         The maximum number of embeddings to be returned.
     * @return A list of embedding matches.
     * Each embedding match includes a relevance score (derivative of cosine distance),
     * ranging from 0 (not relevant) to 1 (highly relevant).
     */
    default List<EmbeddingMatch<Embedded>> findRelevant(Embedding referenceEmbedding, int maxResults) {
        return findRelevant(referenceEmbedding, maxResults, 0);
    }

    /**
     * Finds the most relevant (closest in space) embeddings to the provided reference embedding.
     *
     * @param referenceEmbedding The embedding used as a reference. Returned embeddings should be relevant (closest) to this one.
     * @param maxResults         The maximum number of embeddings to be returned.
     * @param minScore           The minimum relevance score, ranging from 0 to 1 (inclusive).
     *                           Only embeddings with a score of this value or higher will be returned.
     * @return A list of embedding matches.
     * Each embedding match includes a relevance score (derivative of cosine distance),
     * ranging from 0 (not relevant) to 1 (highly relevant).
     */
    List<EmbeddingMatch<Embedded>> findRelevant(Embedding referenceEmbedding, int maxResults, double minScore);

    /**
     * Finds the most relevant (closest in space) embeddings to the provided reference embedding.
     * By default, minScore is set to 0, which means that the results may include embeddings with low relevance.
     * @param memoryId           The memoryId used Distinguishing query requests from different users.
     * @param referenceEmbedding The embedding used as a reference. Returned embeddings should be relevant (closest) to this one.
     * @param maxResults         The maximum number of embeddings to be returned.
     * @return A list of embedding matches.
     * Each embedding match includes a relevance score (derivative of cosine distance),
     * ranging from 0 (not relevant) to 1 (highly relevant).
     */
    default List<EmbeddingMatch<Embedded>> findRelevant(Object memoryId,Embedding referenceEmbedding, int maxResults) {
        throw new RuntimeException("Not implemented");
    }
    /**
     * Finds the most relevant (closest in space) embeddings to the provided reference embedding.
     * @param memoryId           The memoryId used Distinguishing query requests from different users.
     * @param referenceEmbedding The embedding used as a reference. Returned embeddings should be relevant (closest) to this one.
     * @param maxResults         The maximum number of embeddings to be returned.
     * @param minScore           The minimum relevance score, ranging from 0 to 1 (inclusive).
     *                           Only embeddings with a score of this value or higher will be returned.
     * @return A list of embedding matches.
     * Each embedding match includes a relevance score (derivative of cosine distance),
     * ranging from 0 (not relevant) to 1 (highly relevant).
     */
     default List<EmbeddingMatch<Embedded>> findRelevant(Object memoryId,Embedding referenceEmbedding, int maxResults, double minScore){
         throw new RuntimeException("Not implemented");
     }

}
