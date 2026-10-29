SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Tra";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Tra"
( 
    -- Parametros de Entrada
    IN p_IdSolicitudCircular BIGINT DEFAULT 0, 
    --VARIABLES DE SALIDA 
    OUT p_out_TablaSalida "BSCL.Normativa.BD::NOR.SALIDA_NCG380TRA" --Se debe llamar siemte p_out_TablaSalida

)   
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA "BSCL" 
AS
BEGIN
    DECLARE v_FechaInicio,v_FechaTermino DATE DEFAULT '0001-01-01';
    DECLARE v_IdEmpresa,v_Count INT:=0;
    DECLARE v_Version,v_FechaVersion  NVARCHAR(10) DEFAULT '';          

    --Se obtiene la fecha de inicio y fecha termino
   
    SELECT
        CAST(COALESCE(MAX("IdEmpresa"),0) AS INT),
        CAST(COALESCE(MAX("FechaInicio"),'0001-01-01') AS DATE),
        CAST(MAX("FechaTermino") AS DATE)
        INTO v_IdEmpresa,v_FechaInicio,v_FechaTermino
    FROM
    (
        SELECT
            CASE WHEN "TipoParametro"='EMP' THEN "Valor" ELSE NULL END AS "IdEmpresa",
            CASE WHEN "TipoParametro"='FINI' THEN "Valor" ELSE NULL END AS "FechaInicio",
            CASE WHEN "TipoParametro"='FTER' THEN "Valor" ELSE NULL END AS "FechaTermino"
        FROM (SELECT
                "TipoParametro","Valor"
              FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
              WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" IN ('EMP','FINI','FTER'))
    );
        
    IF (:v_FechaInicio = '' OR :v_FechaInicio='0001-01-01') THEN
        v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
    ELSE --Se obtiene segun fecha 
        v_FechaVersion :=TO_VARCHAR (:v_FechaInicio, 'DD-MM-YYYY');
    END IF; 
   
   -- SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"('NCG380TRA',:v_FechaVersion);
    
	p_out_TablaSalida =
    SELECT
    	(CASE WHEN c."TipoDocumentoIdentidad" = 'R' THEN (LTRIM(RTRIM(SUBSTRING(c."DocumentoIdentidad",1,LENGTH(c."DocumentoIdentidad")-2)))) 
							ELSE 0 END) 				AS "Rut",
		(CASE WHEN c."TipoDocumentoIdentidad" = 'R' THEN	(LTRIM(RTRIM(SUBSTRING(c."DocumentoIdentidad",LENGTH(c."DocumentoIdentidad"),LENGTH(c."DocumentoIdentidad")))))
							ELSE '' END) 				AS "DIG",
		(CASE WHEN c."TipoDocumentoIdentidad" = 'P' THEN TRIM(c."DocumentoIdentidad") 
							ELSE '' END) 				AS "Pasaporte",					

		c."ApellidoPaterno" 				AS "ApellidoPaterno",
		c."ApellidoMaterno" 				AS "ApellidoMaterno",
		CASE WHEN SUBSTR_BEFORE (c."Nombres",' ')='' THEN c."Nombres" ELSE SUBSTR_BEFORE (c."Nombres",' ') END  AS "PrimerNombre",
		SUBSTR_AFTER  (c."Nombres",' ') 	AS "SegundoNombre",
		TEL."Numero" 						AS "Telefono",
		DIRE. "Direccion" 					AS "Email",
		'' 							AS "TipoDireccion",
		DIRF."Calle" 						AS "DireccionNombre",
		DIRF."Numero" 						AS "DireccionNumero",
		DIRF."Departamento" 				AS "DireccionDepto",
		DIRF."NombreDivisionPais"			AS "DireccionComuna",
		DIRF."NombreCiudad" 				AS "DireccionCiudad"  ,  
		REG."NombreClasificacionDivisionPais1" AS "DireccionEstado",
		"PAIS"."NombrePais" 				AS "DireccionPais",
		"DIRF"."CodigoPostal" 				AS "DireccionCodPostal",
		"PAIS"."NombrePais" 				AS "Nacionalidad",
		--* MSH
		--* c."FechaInicioCargo" 				AS "FechaIniFunciones",
		CASE WHEN TO_VARCHAR(c."FechaInicioCargo",'YYYY-MM-DD') <= '1900-01-01' THEN c."FechaIngreso" ELSE c."FechaInicioCargo" END	AS "FechaIniFunciones",					
		CASE c."FechaTerminoCargo" WHEN '0001-01-01' THEN '' ELSE c."FechaTerminoCargo"  END AS "FechaTerFunciones",
		c."NombreTipoCargoEmpleado" 		AS "Funcion",
	    EMP."FechaAcreditacion"				AS "FechaAcreditacion",
		CASE WHEN TRIM(c."InformacionPrivilegiada") = 'S' THEN '1' ELSE '0'END AS "InfoPrivilegiada",
		c."NombreEstado"					AS "Estado",
		CASE WHEN c."IndEmpleadoCorredora" = 'S' THEN 'Si' ELSE 'No' END AS "EmpleadoBanco"
	FROM
    (
        SELECT 
          c."DocumentoIdentidad",c."TipoDocumentoIdentidad",c."IdPaisNacimiento",c."IdTelefonoPrincipal",c."IdDireccionFisicaPrincipal",c."IdPersona",c."FechaInicioCargo",c."FechaTerminoCargo",c."NombreTipoCargoEmpleado",c."FechaIngreso"
          ,c."IdDireccionElectronicaPrincipal",c."ApellidoPaterno",c."ApellidoMaterno",c."Nombres",c."NombreEstado", c."IdEmpleado",DET."IndEmpleadoCorredora", DET."InformacionPrivilegiada"
         FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS c
		INNER JOIN (SELECT "Valor","IdSistema" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'RUTEMPL') AS r ON (r."Valor" IS NULL OR r."IdSistema" = c."IdPersona")
    	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoDetalle" AS DET ON (DET."IdEmpleado" = c."IdEmpleado")
    ) AS c
    --homologacion
   	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica"	    AS DIRF ON (DIRF."IdDireccionFisica" = c."IdDireccionFisicaPrincipal")
   	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica"	AS DIRE ON (DIRE."IdDireccionElectronica" = c."IdDireccionElectronicaPrincipal")
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" 											AS PAIS ON (PAIS."IdPais" = c."IdPaisNacimiento") 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" 										AS TEL  ON (TEL."IdTelefono" = c."IdTelefonoPrincipal") 
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"  			AS IND  ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='DERPRIV' AND  c."IdPersona" = IND."IdPersona")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"  			AS INDE ON (INDE."IdUso"='IND' AND INDE."CodigoTipoIndicador"='FEC_ACR' AND  c."IdEmpleado" = INDE."IdEmpleado")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisRegionCiudad" 				AS REG ON (REG."NombreCiudad" = DIRF."NombreCiudad") AND (REG."NombreDivisionPais" = DIRF."NombreDivisionPais")
    LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" 										AS EMP  ON (c."IdPersona" = EMP."IdPersona")
    LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 								AS SOC ON (c."IdPersona" = SOC."IdPersona")
    LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" 										AS PER ON (c."IdPersona" = PER."IdPersona")
    
    WHERE TO_DATE(CASE WHEN TO_VARCHAR(c."FechaInicioCargo",'YYYY-MM-DD') <= '1900-01-01' THEN c."FechaIngreso" ELSE c."FechaInicioCargo" END)  <= TO_DATE(:v_FechaTermino)
    AND SOC."IdEmpleado" > 0 ;    

    --* Mientras se soluciona escritura de Archivos en ANSI (ahora es UTF y excel los lee mal al abrirlos)   
  
    SELECT COUNT(1) INTO v_Count FROM :p_out_TablaSalida;
		
	IF v_Count>0 THEN
	p_out_TablaSalida =
		SELECT	TMP."Rut" AS "Rut"
				,TMP."DIG" AS "DIG"
				,TMP."Pasaporte" AS "Pasaporte"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."ApellidoPaterno") AS "ApellidoPaterno"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."ApellidoMaterno") AS "ApellidoMaterno"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."PrimerNombre") AS "PrimerNombre"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."SegundoNombre") AS "SegundoNombre"
				,TMP."Telefono" AS "Telefono"
				,TMP."Email" AS "Email"
				,TMP."TipoDireccion" AS "TipoDireccion"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."DireccionNombre") AS "DireccionNombre"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."DireccionNumero") AS "DireccionNumero"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."DireccionDepto") AS "DireccionDepto"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."DireccionComuna") AS "DireccionComuna"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."DireccionCiudad") AS "DireccionCiudad"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."DireccionEstado") AS "DireccionEstado"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."DireccionPais") AS "DireccionPais"
				,TMP."DireccionCodPostal" AS "DireccionCodPostal"
				,"BSCL.Normativa.BD.Utilitarios::NOR.SinAcento"(TMP."Nacionalidad") AS "Nacionalidad"
				,TMP."FechaIniFunciones" AS "FechaIniFunciones"
				,TMP."FechaTerFunciones" AS "FechaTerFunciones"
				,TMP."Funcion" AS "Funcion"
				,TMP."FechaAcreditacion" AS "FechaAcreditacion"
				,TMP."InfoPrivilegiada" AS "InfoPrivilegiada"
				,TMP."Estado" AS "Estado"
				,TMP."EmpleadoBanco"  AS "EmpleadoBanco"
		FROM 	:p_out_TablaSalida AS TMP
		ORDER BY to_bigint(REPLACE(TMP."Rut",'.','')) ASC;		
 ELSE
 				p_out_TablaSalida=	select 
							'' as "Rut"				
							,''  as "DIG"			 
							,''  as "Pasaporte"		 
							,''  as "ApellidoPaterno" 
							,''  as "ApellidoMaterno" 
							,''  as "PrimerNombre" 	 
							,''  as "SegundoNombre"	 
							,''  as "Telefono"		 
							,''  as "Email"			 
							,''  as "TipoDireccion"	 
							,''  as "DireccionNombre" 
							,''  as "DireccionNumero" 
							,''  as "DireccionDepto"	
							,''  as "DireccionComuna" 
							,''  as "DireccionCiudad" 
							,''  as "DireccionEstado" 
							,''  as "DireccionPais"	 
							,''  as "DireccionCodPostal"
							,''  as "Nacionalidad"		 
							,''  as "FechaIniFunciones"	
							,''  as "FechaTerFunciones"	
							,''  as "Funcion"			 
							,''  as "FechaAcreditacion"		
							,''  as "InfoPrivilegiada"	
							,''  AS "Estado"
							,''  AS "EmpleadoBanco"
							from dummy where 1=2;
 END IF;
END;