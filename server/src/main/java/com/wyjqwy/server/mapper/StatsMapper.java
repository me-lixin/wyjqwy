package com.wyjqwy.server.mapper;

import com.wyjqwy.server.model.dto.stats.CategoryStatResponse;
import com.wyjqwy.server.model.dto.stats.StatsSummaryResponse;
import com.wyjqwy.server.model.dto.stats.TrendPointResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StatsMapper {
    @Select("""
            SELECT
                COALESCE(SUM(CASE WHEN type = 2 THEN amount END), 0) AS totalIncome,
                COALESCE(SUM(CASE WHEN type = 1 THEN amount END), 0) AS totalExpense,
                COALESCE(SUM(CASE WHEN type = 2 THEN amount END), 0) - COALESCE(SUM(CASE WHEN type = 1 THEN amount END), 0) AS balance
            FROM book_transaction
            WHERE user_id = #{userId} AND occurred_at >= #{from} AND occurred_at < #{to}
            """)
    StatsSummaryResponse summary(@Param("userId") Long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Select("""
            <script>
            SELECT
                <choose>
                    <when test="granularity == 'month'">DATE_FORMAT(occurred_at, '%Y-%m')</when>
                    <otherwise>DATE_FORMAT(occurred_at, '%Y-%m-%d')</otherwise>
                </choose> AS period,
                COALESCE(SUM(amount), 0) AS amount
            FROM book_transaction
            WHERE user_id = #{userId}
              AND occurred_at <![CDATA[>=]]> #{from}
              AND occurred_at <![CDATA[<]]> #{to}
              <if test="type != null">AND type = #{type}</if>
            GROUP BY period
            ORDER BY period
            </script>
            """)
    List<TrendPointResponse> trend(@Param("userId") Long userId, @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to, @Param("type") Integer type,
                                   @Param("granularity") String granularity);

    @Select("""
            SELECT
                t.category_id AS categoryId,
                COALESCE(c.name, '已删除分类') AS categoryName,
                COALESCE(SUM(t.amount), 0) AS amount
            FROM book_transaction t
            LEFT JOIN category c ON c.id = t.category_id
            WHERE t.user_id = #{userId}
              AND t.occurred_at >= #{from}
              AND t.occurred_at < #{to}
              AND t.type = #{type}
            GROUP BY t.category_id, c.name
            ORDER BY amount DESC
            LIMIT #{limit}
            """)
    List<CategoryStatResponse> byCategory(@Param("userId") Long userId, @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to, @Param("type") Integer type,
                                          @Param("limit") Integer limit);
}
