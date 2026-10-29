SELECT
    CASE COL."COLUMN_NAME"
        WHEN 'IdEstado' THEN IFNULL(CAST(CNT."IdEstado" AS NVARCHAR(255)), '')
        WHEN 'CodigoInscripcion' THEN IFNULL(CAST(CNT."CodigoInscripcion" AS NVARCHAR(255)), '')
        WHEN 'Otros' THEN IFNULL(CAST(CNT."Otros" AS NVARCHAR(255)), '')
        WHEN 'Emisor' THEN IFNULL(CAST(CNT."Emisor" AS NVARCHAR(255)), '')
        WHEN 'Depositos' THEN IFNULL(CAST(CNT."Depositos" AS NVARCHAR(255)), '')
        WHEN 'Bolsa' THEN IFNULL(CAST(CNT."Bolsa" AS NVARCHAR(255)), '')
        WHEN 'Boveda' THEN IFNULL(CAST(CNT."Boveda" AS NVARCHAR(255)), '')
        WHEN 'Corredora' THEN IFNULL(CAST(CNT."Corredora" AS NVARCHAR(255)), '')
        WHEN 'CamaraDeCompensacion' THEN IFNULL(CAST(CNT."CamaraDeCompensacion" AS NVARCHAR(255)), '')
        WHEN 'Banco' THEN IFNULL(CAST(CNT."Banco" AS NVARCHAR(255)), '')
        ELSE ''
    END AS "ValorAntiguo",
    IFNULL(COL."DATA_TYPE_NAME", '') AS "TipoDato"
FROM "BSCL.SocioNegocio.BD::CNT.Contraparte" CNT
INNER JOIN SYS.TABLE_COLUMNS COL
    ON COL."SCHEMA_NAME" = :SchemaName
   AND COL."TABLE_NAME" = 'BSCL.SocioNegocio.BD::CNT.Contraparte'
   AND COL."COLUMN_NAME" = :CampoModificado
WHERE CNT."IdContraparte" = :IdContraparte