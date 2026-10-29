

MERGE INTO "BSCL.Custodia.BD::CUS.ItemsFlujoCustodia" AS Target
USING (
    SELECT
        :IdUsoFlujoCustodia AS "IdUsoFlujoCustodia",
        :ItemCalculo AS "ItemCalculo",
        :ItemOrigen AS "ItemOrigen",
        :Descripcion AS "Descripcion",
        :FuncionOperador AS "FuncionOperador",
        :IdEstadoItem AS "IdEstado"
    FROM DUMMY
) AS Source
ON Target."IdUsoFlujoCustodia" = Source."IdUsoFlujoCustodia" 
   AND Target."ItemCalculo" = Source."ItemCalculo"
WHEN MATCHED THEN
    UPDATE SET
        "ItemOrigen" = Source."ItemOrigen",
        "Descripcion" = Source."Descripcion",
        "FuncionOperador" = Source."FuncionOperador",
        "IdEstado" = Source."IdEstado"
WHEN NOT MATCHED THEN
    INSERT (
        "IdUsoFlujoCustodia",
        "ItemCalculo",
        "ItemOrigen",
        "Descripcion",
        "FuncionOperador",
        "IdEstado"
    ) VALUES (
        Source."IdUsoFlujoCustodia",
        Source."ItemCalculo",
        Source."ItemOrigen",
        Source."Descripcion",
        Source."FuncionOperador",
        Source."IdEstado"
    );