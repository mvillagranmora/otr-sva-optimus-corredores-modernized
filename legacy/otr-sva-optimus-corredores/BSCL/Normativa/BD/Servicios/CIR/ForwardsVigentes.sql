SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.ForwardsVigentes"; 
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.ForwardsVigentes" (
 IN p_IdEmpresa                BIGINT,
 IN p_FechaConsulta            NVARCHAR(10),    --'YYYY-MM-DD'
 IN p_cliente                  TABLE("IdCliente" BIGINT,"IdPortafolio" BIGINT),
 IN p_fechaHoraSistema         NVARCHAR(40),
 OUT p_Salida                TABLE(
 "OrdenInterno"                    BIGINT,
 "CodigoPortafolio"                NVARCHAR(40),
 "IdPortafolio"                    BIGINT,
 "Grupo"                           NVARCHAR(40),
 "IdClasificacionTipoInstrumento"  NVARCHAR(10),
 "Nemotecnico"                     NVARCHAR(40),
 "IdMoneda"                        NVARCHAR(3),
 "Cantidad"                        DECIMAL(26,8),
 "ValorPresente"                   DECIMAL(26,8),
 "FechaVencimiento"                NVARCHAR(10),
 "PrecioCierre"                    DECIMAL(26,8),
 "Tasa"                            DECIMAL(26,8),
 "IdCliente"                       BIGINT,
 "PrecioDecimales"                 DECIMAL(26,8),
 "TasaDecimales"                   DECIMAL(26,8)
 )
 )
 LANGUAGE SQLSCRIPT
 SQL SECURITY INVOKER
 DEFAULT SCHEMA "BSCL"
 AS
 BEGIN
 DECLARE v_FechaConsulta     NVARCHAR(10) := '2300-12-31';
 DECLARE v_fechaHoraSistema  NVARCHAR(40);
 DECLARE v_IdEmpresa         BIGINT;
 v_FechaConsulta=p_FechaConsulta;
 v_fechaHoraSistema = p_fechaHoraSistema;
 v_IdEmpresa=p_IdEmpresa;
 v_cliente =   SELECT "IdCliente", "IdPortafolio" FROM  :p_cliente;
 v_decimales = SELECT MF."PrecioDecimales"
                     ,MF."TasaDecimales"
                     ,MF."IdMoneda"
               FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
               WHERE  MF."IdTipoInstrumento" = 'FWD'
               AND MF."IdUsoMonedaFormato" = 'CUS'
               AND MF."IdTipoOperacion" ='';
 IF  TO_VARCHAR(v_FechaConsulta, 'YYYY-MM') <> TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM') THEN
 
 v_detalle = SELECT "FechaVencimientoOperacion", "FolioComprobante", "FechaRespaldo", "IdCartera" FROM "BSCL.Custodia.BD::HIS.Cartera" WHERE "IdTipoInstrumento" = 'FWD';
 p_Salida =  SELECT
    130 AS "OrdenInterno"
    ,CP."CodigoPortafolio" as "CodigoPortafolio"
    ,CP."IdPortafolio" as "IdPortafolio"
    ,'FORWARD - ' || COALESCE(INSTR."RazonSocial",'') AS "Grupo"
    --ESPECIFICOS
    ,COALESCE(CAR."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
    ,(CASE WHEN OPE."AccionOperacion" = 'COMPRA' THEN 'VENTA' ELSE 'COMPRA' END) AS "Nemotecnico" 
    ,COALESCE(CAR."IdMonedaPrincipal",'')  AS "IdMoneda"
    ,CAR."Cantidad" AS "Cantidad"
    ,0 AS "ValorPresente" --Evaluar
    ,TO_VARCHAR (COALESCE(CARPAC."FechaVencimientoOperacion",''), 'DD/MM/YYYY')"FechaVencimiento"
    ,(CASE WHEN CAR."ValorParidadCierre" > 0 THEN CAR."ValorParidadCierre" ELSE CAR."ValorTipoCambioCierre" END)   AS "PrecioCierre"
    ,0 AS "Tasa"
    ,CAR."IdCliente"
    ,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
	 ,COALESCE(MF."TasaDecimales" , 0)  AS "TasaDecimales"
    FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
    INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA ON (CLA."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento")
    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON (CP."IdCliente" = CAR."IdCliente" AND CP."IdPortafolio" = CAR."IdPortafolio")
    INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE ON CAR."IdTipoOperacion" = OPE."IdTipoOperacion"
    INNER JOIN :v_detalle AS CARPAC ON CARPAC."FolioComprobante" = CAR."FolioComprobante" AND CARPAC."FechaRespaldo" = CAR."FechaRespaldo"
    INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INSTR ON INSTR."IdTipoInstrumento" = CAR."IdTipoInstrumento" AND INSTR."Nemotecnico" = CAR."Nemotecnico"
    LEFT JOIN :v_decimales AS MF ON MF."IdMoneda" = CAR."IdMonedaPrincipal"
    WHERE CLA."IdEstado"='ACT'
    AND TO_DATE(CAR."FechaOperacion") <=TO_DATE(:p_FechaConsulta)
    AND  CAR."FechaRespaldo" = :v_FechaConsulta
    AND  CAR."IdTipoOperacion" IN ('COMFWD','VENFWD')
    AND CAR."IdCliente" IN (SELECT DISTINCT "IdCliente" FROM :v_cliente)
    AND CAR."IdPortafolio" IN (SELECT DISTINCT "IdPortafolio" FROM :v_cliente)
    AND CAR."IdEmpresa" = :v_IdEmpresa
    AND CAR."IdEstado" = 'VIG';
 ELSE
 v_detalle = SELECT "FechaVencimientoOperacion", "FolioComprobante", "INGFechaOperacion" as "FechaRespaldo", "IdCartera" FROM "BSCL.Custodia.BD::CUS.Cartera" WHERE "IdTipoInstrumento" = 'FWD';
 p_Salida =  SELECT
    130 AS "OrdenInterno"
    ,CP."CodigoPortafolio" as "CodigoPortafolio"
    ,CP."IdPortafolio" as "IdPortafolio"
    ,'FORWARD - ' || COALESCE(INSTR."RazonSocial",'') AS "Grupo"
    --ESPECIFICOS
    ,COALESCE(CAR."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
    ,(CASE WHEN OPE."AccionOperacion" = 'COMPRA' THEN 'VENTA' ELSE 'COMPRA' END) AS "Nemotecnico" 
    ,COALESCE(CAR."IdMonedaPrincipal",'')  AS "IdMoneda"
    ,CAR."Cantidad" AS "Cantidad"
    ,0 AS "ValorPresente" --Evaluar
    ,TO_VARCHAR (COALESCE(CARPAC."FechaVencimientoOperacion",''), 'DD/MM/YYYY')"FechaVencimiento"
    ,(CASE WHEN CAR."ValorParidadCierre" > 0 THEN CAR."ValorParidadCierre" ELSE CAR."ValorTipoCambioCierre" END)   AS "PrecioCierre"
    ,0 AS "Tasa"
    ,CAR."IdCliente"
    ,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
	 ,COALESCE(MF."TasaDecimales" , 0)  AS "TasaDecimales"
    FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
    INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA ON (CLA."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento")
    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON (CP."IdCliente" = CAR."IdCliente" AND CP."IdPortafolio" = CAR."IdPortafolio")
    INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE ON CAR."IdTipoOperacion" = OPE."IdTipoOperacion"
    INNER JOIN :v_detalle AS CARPAC ON CARPAC."FolioComprobante" = CAR."FolioComprobante" AND CARPAC."IdCartera" = CAR."IdCartera"
    INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INSTR ON INSTR."IdTipoInstrumento" = CAR."IdTipoInstrumento" AND INSTR."Nemotecnico" = CAR."Nemotecnico"
    LEFT JOIN :v_decimales AS MF ON MF."IdMoneda" = CAR."IdMonedaPrincipal"
    WHERE CLA."IdEstado"='ACT'
    AND TO_DATE(CAR."FechaOperacion"  ) <=TO_DATE(:p_FechaConsulta)
    AND CAR."IdTipoOperacion" IN ('COMFWD','VENFWD')
    AND CAR."IdCliente" IN (SELECT DISTINCT "IdCliente" FROM :v_cliente)
    AND CAR."IdPortafolio" IN (SELECT DISTINCT "IdPortafolio" FROM :v_cliente)
    AND CAR."IdEmpresa" = :p_idEmpresa
    AND CAR."IdEstado" = 'VIG';
 END IF;

END;

