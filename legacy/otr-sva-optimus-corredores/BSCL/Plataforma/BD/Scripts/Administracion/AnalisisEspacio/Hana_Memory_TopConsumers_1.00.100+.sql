SELECT
/* 

[NAME]

- HANA_Memory_TopConsumers_1.00.100+

[DESCRIPTION]

- Areas with highest memory requirements (tables, heap, ...)

[SOURCE]

- SAP Note 1969700

[DETAILS AND RESTRICTIONS]

- INSTANCE_CODE_SIZE only available as of revision 60
- Proper CODE calculation does not work with Rev. <= 84 due to bug 64150
- M_HEAP_MEMORY.COMPONENT available starting with SAP HANA 1.0 SPS 10
- See SAP Note 1999997 and 2388483 for memory reduction approaches
- Can be used for monitoring remote system replication sites, see SAP Note 1999880 
  ("Is it possible to monitor remote system replication sites on the primary system") for details.
  Only works for AREA = 'HEAP'

[VALID FOR]

- Revisions:              >= 1.00.100
- Statistics server type: all

[SQL COMMAND VERSION]

- 2014/03/06:  1.0 (initial version)
- 2014/11/01:  1.1 (ORDER_BY included)
- 2015/03/09:  1.2 (row store fragmentation included)
- 2015/04/27:  1.3 (ONLY_SQL_DATA_AREAS included)
- 2016/01/20:  1.4 (dedicated version for Rev. 85 and higher, proper CODE calculation)
- 2017/01/11:  1.5 (SUBAREA included)
- 2017/03/16:  1.6 (SUBAREA modified)
- 2017/10/19:  1.7 (dedicated 1.00.100+ version)

[INVOLVED TABLES]

- M_HEAP_MEMORY
- M_CS_TABLES
- M_RS_TABLES
- M_RS_INDEXES

[INPUT PARAMETERS]

- HOST

  Host name

  'saphana01'     --> Specic host saphana01
  'saphana%'      --> All hosts starting with saphana
  '%'             --> All hosts

- PORT

  Port number

  '30007'         --> Port 30007
  '%03'           --> All ports ending with '03'
  '%'             --> No restriction to ports

- AREA

  Memory area

  'ROW'           --> Row store
  'COLUMN'        --> Column store
  'HEAP'          --> Heap area
  '%'             --> No memory area restriction

- SUBAREA

  Memory sub area (part of row / column store or heap component)

  'ROWTAB'        --> Row store tables
  'ROWIND'        --> Row store indexes
  'COLMAIN'       --> Column store main storage
  'COLDELTA'      --> Column store delta storage
  'Cache'         --> Heap component 'Cache'
  '%'             --> No restriction related to sub area

- DETAIL

  Memory area detail (e.g. table name, heap area)

  'KONV'          --> Information related to detail KONV (table name on column store)
  '%'             --> No restriction to memory area details

- ONLY_SQL_DATA_AREAS

  Possibility to restrict results to heap areas linked to SQL statement data processing

  'X'             --> Only display heap areas linked to SQL statement data processing
  ' '             --> No restriction related to SQL statement data processing areas

- RESULT_ROWS

  Number of records to be returned by the query

  100             --> Return a maximum number of 100 records
  -1              --> Return all records

- MIN_SIZE_GB

  Threshold for minimum size (GB)

  5               --> Only display areas allocating at least 5 GB of memory
  -1              --> No minimum size restriction

- ORDER_BY

  Sort criteria (available values are provided in comment)

  'NUM'           --> Sorting by number of pieces
  'SIZE'          --> Sorting by size

- AGGREGATE_BY

  Aggregation criteria (possible values can be found in comment)

  'DETAIL'        --> Aggregation by detail
  'HOST, PORT'    --> Aggregation by host and port
  'NONE'          --> No aggregation

- INCLUDE_OVERLAPPING_HEAP_AREAS

  Controls the display of heap areas that are already covered differently (e.g. as part of column store, row store or stack)

  'X'             --> Overlapping heap areas are included (memory can be accounted twice)
  ' '             --> Overlapping heap areas are excluded
  
[OUTPUT PARAMETERS]

- HOST:                        Host name
- PORT:                        Port
- AREA:                        Memory area (e.g. column store, row store, heap)
- SUBAREA:                     Memory sub area (e.g. column store main storage or heap component)
- DETAIL:                      Memory area detail (e.g. table name, heap area)
- COUNT:                       Number of memory objects involved
- GAL_GB:                      Global allocation limit (sum of all selected hosts) (GB)
- SIZE_GB:                     Memory size (GB)
- SIZE_PCT:                    Memory size compared to global allocation limit (%)

[EXAMPLE OUTPUT]

--------------------------------------------------------------------------------------------------------------------
|HOST|PORT |AREA  |SUBAREA                                          |DETAIL|COUNT   |GAL_GB    |SIZE_GB   |SIZE_PCT|
--------------------------------------------------------------------------------------------------------------------
|any |  any|COLUMN|Column Store (Main)                              |any   |  119789|  15664.80|   9369.12|   59.81|
|any |  any|HEAP  |Heap (Memory Fragmentation)                      |any   |       6|  15664.80|   1910.84|   12.19|
|any |  any|HEAP  |Heap (System)                                    |any   |    1848|  15664.80|    602.34|    3.84|
|any |  any|ROW   |Row Store (Tables)                               |any   |    3790|  15664.80|    483.54|    3.08|
|any |  any|HEAP  |Heap (Monitoring & Statistical Data)             |any   |     140|  15664.80|    444.25|    2.83|
|any |  any|HEAP  |Heap (System - Page Cache)                       |any   |       3|  15664.80|    435.75|    2.78|
|any |  any|HEAP  |Heap (Column Store Tables)                       |any   |      14|  15664.80|    193.74|    1.23|
|any |  any|ROW   |Row Store (Ext. Fragmentation)                   |any   |       2|  15664.80|    184.45|    1.17|
|any |  any|COLUMN|Column Store (Delta)                             |any   |  119789|  15664.80|    140.59|    0.89|
|any |  any|HEAP  |Heap (Statement Execution & Intermediate Results)|any   |     911|  15664.80|    130.26|    0.83|
|any |  any|HEAP  |Heap (Caches)                                    |any   |      28|  15664.80|    122.35|    0.78|
|any |  any|ROW   |Row Store (Int. Fragmentation)                   |any   |       1|  15664.80|     53.88|    0.34|
|any |  any|ROW   |Row Store (Indexes)                              |any   |    1501|  15664.80|     39.22|    0.25|
|any |  any|HEAP  |Heap (Row Store Tables)                          |any   |      19|  15664.80|     15.91|    0.10|
|any |  any|STACK |Stack                                            |any   |       6|  15664.80|      2.37|    0.01|
|any |  any|CODE  |Code                                             |any   |       6|  15664.80|      1.99|    0.01|
|any |  any|COLUMN|Column Store (Others)                            |any   |  119789|  15664.80|      0.00|    0.00|
--------------------------------------------------------------------------------------------------------------------

*/

  HOST,
  LPAD(PORT, 5) PORT,
  AREA,
  SUBAREA,
  DETAIL,
  LPAD(NUM_OBJECTS, 8) COUNT,
  LPAD(TO_DECIMAL(GAL_GB, 10, 2), 10) GAL_GB,
  LPAD(TO_DECIMAL(SIZE_GB, 10, 2), 10) SIZE_GB,
  LPAD(TO_DECIMAL(MAP(GAL_GB, 0, 0, SIZE_GB / GAL_GB * 100), 10, 2), 8) SIZE_PCT
FROM
( SELECT
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'HOST')    != 0 THEN HOST             ELSE MAP(BI_HOST,    '%', 'any', BI_HOST)    END HOST,
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'PORT')    != 0 THEN TO_VARCHAR(PORT) ELSE MAP(BI_PORT,    '%', 'any', BI_PORT)    END PORT,
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'AREA')    != 0 THEN AREA             ELSE MAP(BI_AREA,    '%', 'any', BI_AREA)    END AREA,
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'SUBAREA') != 0 THEN SUBAREA          ELSE MAP(BI_SUBAREA, '%', 'any', BI_SUBAREA) END SUBAREA,
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'DETAIL')  != 0 THEN DETAIL           ELSE 'any'                                   END DETAIL,
    COUNT(*) NUM_OBJECTS,
    SUM(SIZE_BYTE) / 1024 / 1024 / 1024 SIZE_GB,
    GAL_GB,
    ROW_NUMBER () OVER (ORDER BY MAP(ORDER_BY, 'NUM', COUNT(*), SUM(SIZE_BYTE)) DESC) ROW_NUM,
    RESULT_ROWS,
    AGGREGATE_BY,
    MIN_SIZE_GB
  FROM
  ( SELECT
      D.HOST,
      D.PORT,
      D.AREA,
      D.SUBAREA,
      D.DETAIL,
      D.SIZE_BYTE,
      ( SELECT SUM(ALLOCATION_LIMIT) / 1024 / 1024 / 1024 GAL_GB FROM M_HOST_RESOURCE_UTILIZATION H WHERE H.HOST LIKE BI.HOST ) GAL_GB,
      BI.RESULT_ROWS,
      BI.AGGREGATE_BY,
      BI.MIN_SIZE_GB,
      BI.HOST BI_HOST,
      BI.PORT BI_PORT,
      BI.AREA BI_AREA,
      BI.SUBAREA BI_SUBAREA,
      BI.DETAIL BI_DETAIL,
      BI.ONLY_SQL_DATA_AREAS,
      BI.ORDER_BY
    FROM
    ( SELECT                                          /* Modification section */
        '%' HOST,
        '%' PORT,
        '%' AREA,                                  /* %, ROW, COLUMN, TABLES (for both row and column store), HEAP, CODE, STACK */
        '%' SUBAREA,                  /* 'Row Store (Tables)', 'Row store (Indexes)', 'Row Store (Int. Fragmentation)', 'Row Store (Ext. Fragmentation)', 'Column Store (Main)', 'Column Store (Delta)', 'Column Store (Others)' or 'Heap (<component>)' */
        '%' DETAIL,
        ' ' ONLY_SQL_DATA_AREAS,
        50 RESULT_ROWS,
        -1 MIN_SIZE_GB,
        'NONE' AGGREGATE_BY,                          /* HOST, PORT, AREA, SUBAREA, DETAIL or comma separated combinations, NONE for no aggregation */
        'SIZE' ORDER_BY,                               /* NUM, SIZE */
        ' ' INCLUDE_OVERLAPPING_HEAP_AREAS
      FROM
        DUMMY
    ) BI,
    ( SELECT
        HOST,
        PORT,
        'HEAP' AREA,
        'Heap' || CHAR(32) || '(' || COMPONENT || 
          CASE WHEN CATEGORY LIKE 'Pool/PersistenceManager/PersistentSpace/DefaultLPA%/Page' THEN CHAR(32) || '-' || CHAR(32) || 'Page Cache' ELSE '' END || ')' SUBAREA,
        CATEGORY DETAIL,
        EXCLUSIVE_SIZE_IN_USE SIZE_BYTE
      FROM
        M_HEAP_MEMORY
      UNION ALL
      ( SELECT
          S.HOST,
          S.PORT,
          'CODE' AREA,
          'Code' SUBAREA,
          'Code' DETAIL,
          TO_DECIMAL(ROUND(H.INSTANCE_CODE_SIZE / COUNT(*) OVER (PARTITION BY S.HOST)), 15, 0) SIZE_BYTE
        FROM
          M_SERVICE_MEMORY S,
          M_HOST_RESOURCE_UTILIZATION H
        WHERE
          S.HOST = H.HOST
      )
      UNION ALL
      ( SELECT
          HOST,
          PORT,
          'STACK' AREA,
          'Stack' SUBAREA,
          'Stack' DETAIL,
          STACK_SIZE SIZE_BYTE
        FROM
          M_SERVICE_MEMORY
      )
      UNION ALL
      ( SELECT
          HOST,
          PORT,
          'ROW',
          'Row Store (Tables)',
          TABLE_NAME,
          SUM(USED_FIXED_PART_SIZE + USED_VARIABLE_PART_SIZE )
        FROM
          M_RS_TABLES
        GROUP BY
          HOST,
          PORT,
          TABLE_NAME
      )
      UNION ALL
      ( SELECT
          HOST,
          PORT,
          'ROW',
          'Row Store (Indexes)',
          TABLE_NAME,
          SUM(INDEX_SIZE )
        FROM
          M_RS_INDEXES
        GROUP BY
          HOST,
          PORT,
          TABLE_NAME
      )
      UNION ALL
      ( SELECT
          HOST,
          PORT,
          'ROW',
          'Row Store (Ext. Fragmentation)',
          'Row Store (Ext. Fragmentation)',
          SUM(FREE_SIZE)
        FROM
          M_RS_MEMORY
        GROUP BY
          HOST,
          PORT
      )
      UNION ALL
      ( SELECT
          HOST,
          PORT,
          'ROW',
          'Row Store (Int. Fragmentation)',
          'Row Store (Int. Fragmentation)',
          SUM(ALLOCATED_FIXED_PART_SIZE + ALLOCATED_VARIABLE_PART_SIZE - USED_FIXED_PART_SIZE - USED_VARIABLE_PART_SIZE)
        FROM
          M_RS_TABLES
        GROUP BY
          HOST,
          PORT
      )
      UNION ALL
      ( SELECT
          CT.HOST,
          CT.PORT,
          'COLUMN',
          L.SUBAREA,
          CT.TABLE_NAME,
          CASE L.SUBAREA
            WHEN 'Column Store (Others)' THEN CT.MEMORY_SIZE_IN_TOTAL - CT.MEMORY_SIZE_IN_MAIN - CT.MEMORY_SIZE_IN_DELTA
            WHEN 'Column Store (Main)'   THEN CT.MEMORY_SIZE_IN_MAIN
            WHEN 'Column Store (Delta)'  THEN CT.MEMORY_SIZE_IN_DELTA
          END USED_BYTE
        FROM
        ( SELECT 'Column Store (Others)' SUBAREA FROM DUMMY UNION ALL
          SELECT 'Column Store (Main)'           FROM DUMMY UNION ALL
          SELECT 'Column Store (Delta)'          FROM DUMMY
        ) L,
          M_CS_TABLES CT
      )
    ) D
    WHERE
      D.HOST LIKE BI.HOST AND
      TO_VARCHAR(D.PORT) LIKE BI.PORT AND
      ( D.AREA LIKE BI.AREA OR
        BI.AREA = 'TABLES' AND D.AREA IN ( 'ROW', 'COLUMN' )
      ) AND
      D.SUBAREA LIKE BI.SUBAREA AND
      D.DETAIL LIKE BI.DETAIL AND
      ( BI.INCLUDE_OVERLAPPING_HEAP_AREAS = 'X' OR
        ( D.DETAIL NOT LIKE 'Pool/AttributeEngine%' AND
          D.DETAIL NOT LIKE 'Pool/ColumnStore%' AND
          D.DETAIL NOT IN
          ( 'Pool/malloc/libhdbcstypes.so',
            'Pool/NameIdMapping/RoDict',
            'Pool/RowEngine/CpbTree',
            'Pool/RowStoreTables/CpbTree',
            'StackAllocator'
          )
        )
      ) AND
      ( BI.ONLY_SQL_DATA_AREAS = ' ' OR
        D.DETAIL LIKE 'Pool/Text/AEText%' OR
        D.DETAIL IN 
        ( 'Pool/AttributeEngine/Transient',
          'Pool/AttributeEngine/Transient/updateContainerConcat',
          'Pool/CSPlanExecutor/PlanExecution',
          'Pool/itab',
          'Pool/itab/VectorColumn',
          'Pool/JoinEvaluator',
          'Pool/JoinEvaluator/JEAssembleResults',
          'Pool/JoinEvaluator/JEAssembleResults/Results',
          'Pool/JoinEvaluator/JECalculate',
          'Pool/JoinEvaluator/JECalculate/Results',
          'Pool/JoinEvaluator/JECreateNTuple',
          'Pool/JoinEvaluator/JEPlanData/deserialized',
          'Pool/JoinEvaluator/JEPreAggregate',
          'Pool/JoinEvaluator/JERequestedAttributes/Results',
          'Pool/JoinEvaluator/JEStep1',
          'Pool/JoinEvaluator/JEStep2',
          'Pool/JoinEvaluator/NTuple',
          'Pool/JoinEvaluator/PlanDataAttrVals/Deserialized',
          'Pool/JoinEvaluator/ValueList',
          'Pool/malloc/libhdbevaluator.so',
          'Pool/malloc/libhdbolap.so',
          'Pool/mds',
          'Pool/parallel/aggregates', 
          'Pool/parallel/align', 
          'Pool/parallel/compactcol', 
          'Pool/parallel/ihm',
          'Pool/parallel/pop',
          'Pool/parallel/temp_aggregates',
          'Pool/parallel/temp_dimensions',
          'Pool/parallel/temp_other',
          'Pool/RowEngine/LOB',
          'Pool/RowEngine/MonitorView',
          'Pool/RowEngine/QueryExecution', 
          'Pool/SearchAPI',
          'Pool/SearchAPI/Itab Search',
          'Pool/StringContainer',
          'Pool/ValueArray',
          'Pool/XDictData' 
        )
      )
  )
  GROUP BY
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'HOST')    != 0 THEN HOST             ELSE MAP(BI_HOST,    '%', 'any', BI_HOST)    END,
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'PORT')    != 0 THEN TO_VARCHAR(PORT) ELSE MAP(BI_PORT,    '%', 'any', BI_PORT)    END,
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'AREA')    != 0 THEN AREA             ELSE MAP(BI_AREA,    '%', 'any', BI_AREA)    END,
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'SUBAREA') != 0 THEN SUBAREA          ELSE MAP(BI_SUBAREA, '%', 'any', BI_SUBAREA) END,
    CASE WHEN AGGREGATE_BY = 'NONE' OR INSTR(AGGREGATE_BY, 'DETAIL')  != 0 THEN DETAIL           ELSE 'any'                                   END,
    AGGREGATE_BY,
    RESULT_ROWS,
    MIN_SIZE_GB,
    GAL_GB,
    ORDER_BY
)
WHERE
  ( MIN_SIZE_GB = -1 OR SIZE_GB >= MIN_SIZE_GB ) AND
  ( RESULT_ROWS = -1 OR ROW_NUM <= RESULT_ROWS )
ORDER BY
  ROW_NUM