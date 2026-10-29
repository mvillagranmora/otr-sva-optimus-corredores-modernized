SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFMMovtosSpot"; 

-- select *  from "BSCL.Replica.BD.Vistas::ReplicaFMMovtosSpot"  where FOLIOTRANSACCION = 1304

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFMMovtosSpot" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	CAST(SPT.FOLIOTRANSACCION AS varchar)  AS TABLE_KEY,
                "FOLIOTRANSACCION", --INT
                "FOLIOOPERACION", --INT
                "FECHAOPERACION", --DATETIME
                "CODFONDO", --CHAR(10)
                "RUTCLIENTE", --CHAR(11)
                "CUENTACLIENTE", --CHAR(2)
                "CODLIBRO", --INT
                "CODCARTERA", --INT
                "CODSUBCARTERA", --INT
                "CODCARTFINANCIERA", --INT
                "CODDEPTO", --INT
                "CODPRODUCTO", --INT
                "CODSUBPRODUCTO", --INT
                "CODTIPOMOVTO", --INT
                "CODENTIDAD", --INT
                "CODSUCURSAL", --INT
                "CODMONPRINC", --INT
                "CODMONSECU", --INT
                "MTOMONPRINC", --DECIMAL(18,4)
                "PARIDADCIERREMONPRINC", --DECIMAL(20,10)
                "PARIDADCIERREMONSECU", --DECIMAL(20,10)
                "TCCIERRE", --DECIMAL(20,10)
                "MTOMONSECU", --DECIMAL(18,4)
                "PARIDADTRANSFMONPRINC", --DECIMAL(20,10)
                "PARIDADTRANSFMONSECU", --DECIMAL(20,10)
                "TCTRANSF", --DECIMAL(20,10)
                "PARIDADHOYMONPRINC", --DECIMAL(20,10)
                "PARIDADHOYMONSECU", --DECIMAL(20,10)
                "TCHOY", --DECIMAL(20,10)
                "CODTCREFERENCIA", --INT
                "TCREFERENCIA", --DECIMAL(20,10)
                "CODPLAZA", --INT
                "CODCURVAMONPRINC", --INT
				"CODCURVAMONSECU",
                "MTOTRANSFERENCIA", --DECIMAL(18,4)
                "MTOCOMISION", --DECIMAL(18,4)
                "MTORENTABILIDAD", --DECIMAL(18,4)
                "INDVIENENVAMOS", --CHAR(1)
                "CODCOMERCIO", --CHAR(6)
                "CODOMA", --CHAR(6)
                "OBSERVACIONES", --VARCHAR(100)
                "ORIGEN", --VARCHAR(20)
                "FOLIOINTERFAZ",--VARCHAR(30)
                "AGENTECOMERCIAL",--VARCHAR(15)
                "ESTADO",--CHAR(1)
                "ESTADOLIMITE", --CHAR(1)
                "ESTADOCONFIRMADOR", --CHAR(1)
                "ESTADOCTLFINANC", --CHAR(1)
                "MARCA1", --VARCHAR(50)
                "MARCA2", --VARCHAR(50)
                "MARCA3", --VARCHAR(50)
                "MARCA4", --VARCHAR(50)
                "MARCA5", --VARCHAR(50)
                "TIPOPAGO", --CHAR(2)
                "USUARIOCREADOR", --VARCHAR(15)
                "FECHACREACION", --DATETIME
                "TERMINAL", --CHAR(16)
                "CODEVENTOTRANS", --INT
                "TCREFCOMPRA", --DECIMAL(20,10)
                "TCREFVENTA", --DECIMAL(20,10)
                "CODSUBLIBRO", --INT
                "CODLIBRO1", --INT
                "CODSUBLIBRO1", --INT
                "CODSUBSUBLIBRO", --INT
                "MTOARBITRAJEMMD", --DECIMAL(18,4)
                "TCCOSTOMMD", --DECIMAL(20,10)
                "TCCIERREMMD", --DECIMAL(20,10)
                "PARIDADCOSTOMMD", --DECIMAL(20,10)
                "PARIDADCIERREMMD", --DECIMAL(20,10)
                "MTOEQUIVALENTEMMD", --DECIMAL(18,4)
                "MTOMARGENMMD", --DECIMAL(18,4)
                "CODTIPOPOSICION", --INT
                "CODAGENTE", --CHAR(4)
                "TIPOCOMPPAGO", --CHAR(10)
                "CODCONCEPTOSII", --INT
                "CODMONCOSTO", --INT
                "COSTOSPOT", --DECIMAL(18,4)
                "TCTRANSFCRM", --DECIMAL(20,10)
                "MTORENTABILIDADCRM", --DECIMAL(18,4)
                "MERCADODIVISAS", --CHAR(1)
                "ESTADOCAJA", --CHAR(1)
                "FOLIOPLANILLA", --NUMERIC(8,0)
                "TIPOGASTO", --CHAR(3)
                "MONTOGASTO", --DECIMAL(13,4)
                "MTOMONSECUINI", --DECIMAL(18,4)
                "MTOTRANSFERENCIAINI", --DECIMAL(18,4)
                "TCCIERREINI", --DECIMAL(20,10)
                "SPREAD", --DECIMAL(20,10)
                "TCSPOT" TCSPOT, --DECIMAL(20,10)
                "PRECIOLIQUIDACION", --DECIMAL(20,10)
                "MTOLIQUIDACION", --DECIMAL(18,4)
                "MOVTOLIQUIDACION", --DECIMAL(8,0)
                "MOVTOCAJA", --DECIMAL(8,0)
                "DOLAROBSERVADO", --BIGINT
                "AFECTOADERIVADOS" --BIGINT

        FROM "BSCL.Replica.BD.Vistas::Sebra.FMMovtosSpot" SPT
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FMMovtosSpot' AND WST."Key" = SPT.FOLIOTRANSACCION


        
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFMMovtosSpot" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
