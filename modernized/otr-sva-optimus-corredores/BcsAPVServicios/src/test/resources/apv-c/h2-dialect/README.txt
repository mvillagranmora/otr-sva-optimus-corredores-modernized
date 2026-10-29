H2 variants of HANA statements that need a structural rewrite (support/H2Hana.java applies the simple textual
rewrites itself; see its Javadoc for the list). NONE has been needed so far for the APV family C statements on the modern side (the one dialect accommodation of family C is oracle-only:
legacy-oracle/resources-override/cl/bcs/APV/sql/ConsultarBonificacionAEjecutar.sql, documented in familia_c/README.md; the modern code does not read the legacy SQL).

File format (one file per statement, listed in index.txt):
  line 1:  -- HANA-SOURCE: cl/bcs/APV/sql/<file>.sql   (the legacy classpath path; the oracle reads it from the legacy sources,
           the modern run from the byte-identical copy apv-c/hana-sql/<file>.sql, which record.sh checks)
  rest:    the H2 statement, using the SAME :named parameters in the SAME order as the HANA source

H2Hana matches the whitespace/comment-normalised HANA text of the statement that reaches the JDBC driver (legacy
cl.bcs.utils.SQL and Spring's NamedParameterJdbcTemplate both hand over the "?" form) and runs the H2 variant instead.
A variant must be semantically identical to the HANA statement for the seeded data; the reason of every rewrite is written
as an SQL comment in the file. List every file in index.txt.
