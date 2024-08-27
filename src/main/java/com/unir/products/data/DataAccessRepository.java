package com.unir.products.data;

import java.net.InetAddress;
import java.util.*;

import com.unir.products.model.db.Product;
import com.unir.products.model.response.AggregationDetails;
import com.unir.products.model.response.ProductsQueryResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DataAccessRepository {

    @Value("${server.fullAddress}")
    private String serverFullAddress;

    // Esta clase (y bean) es la unica que usan directamente los servicios para
    // acceder a los datos.
    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticClient;

    private final String[] descriptionSearchFields = {"description", "description._2gram", "description._3gram"};

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Boolean delete(Product product) {
        productRepository.delete(product);
        return Boolean.TRUE;
    }

	public Optional<Product> findById(String id) {
		return productRepository.findById(id);
	}

    @SneakyThrows
    public ProductsQueryResponse findProducts(String title, String category, String description, Boolean aggregate) {

        BoolQueryBuilder querySpec = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(category)) {
            querySpec.must(QueryBuilders.matchQuery("category", category));
        }

        if (!StringUtils.isEmpty(title)) {
            querySpec.must(QueryBuilders.matchQuery("title", title));
        }

        if (!StringUtils.isEmpty(description)) {
            querySpec.must(QueryBuilders.multiMatchQuery(description, descriptionSearchFields).type(Type.BOOL_PREFIX));
        }

        //Si no he recibido ningun parametro, busco todos los elementos.
        if (!querySpec.hasClauses()) {
            querySpec.must(QueryBuilders.matchAllQuery());
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(querySpec);

        if (aggregate) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("Category Aggregation").field("category").size(1000));
            nativeSearchQueryBuilder.withMaxResults(0);
        }

        //Opcionalmente, podemos paginar los resultados
        //nativeSearchQueryBuilder.withPageable(PageRequest.of(0, 10));

        Query query = nativeSearchQueryBuilder.build();
        SearchHits<Product> result = elasticClient.search(query, Product.class);

        List<AggregationDetails> responseAggs = new LinkedList<>();

        if (result.hasAggregations()) {
            Map<String, Aggregation> aggs = result.getAggregations().asMap();
            ParsedStringTerms countryAgg = (ParsedStringTerms) aggs.get("Category Aggregation");

            //Componemos una URI basada en serverFullAddress y query params para cada argumento, siempre que no viniesen vacios
            String queryParams = getQueryParams(title, category, description);
            countryAgg.getBuckets()
                    .forEach(
                            bucket -> responseAggs.add(
                                    new AggregationDetails(
                                            bucket.getKey().toString(),
                                            (int) bucket.getDocCount(),
                                            serverFullAddress + "/products?category=" + bucket.getKey() + queryParams)));
        }
        return new ProductsQueryResponse(result.getSearchHits().stream().map(SearchHit::getContent).toList(), responseAggs);
    }

    /**
     * Componemos una URI basada en serverFullAddress y query params para cada argumento, siempre que no viniesen vacios
     *
     * @param name        - nombre del producto
     * @param description - descripcion del producto
     * @param country     - pais del producto
     * @return
     */
    private String getQueryParams(String title, String category, String description) {
        String queryParams = (StringUtils.isEmpty(title) ? "" : "&title=" + title)
                + (StringUtils.isEmpty(description) ? "" : "&description=" + description);
        // Eliminamos el ultimo & si existe
        return queryParams.endsWith("&") ? queryParams.substring(0, queryParams.length() - 1) : queryParams;
    }
}
