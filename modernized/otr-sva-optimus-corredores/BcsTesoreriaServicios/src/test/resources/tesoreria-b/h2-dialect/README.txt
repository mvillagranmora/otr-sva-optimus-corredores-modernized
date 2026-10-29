H2 variants of HANA statements that need a structural rewrite (support/H2Hana.java applies the simple textual
rewrites itself; see its Javadoc for the list).

File format:
  line 1:  -- HANA-SOURCE: cl/bcs/tesoreria/sql/<file>.sql (the legacy classpath path; the modern copy is identical)
  rest:    the H2 statement, using the SAME :named parameters in the SAME order as the HANA source

H2Hana matches the whitespace/comment-normalised HANA text of the statement that reaches the JDBC driver (legacy
cl.bcs.utils.SQL and Spring's NamedParameterJdbcTemplate both hand over the "?" form) and runs the H2 variant instead.
A variant must be semantically identical to the HANA statement for the seeded data; the reason of every rewrite is written
as an SQL comment in the file. List every file in index.txt.
