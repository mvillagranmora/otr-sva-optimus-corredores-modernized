SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.CrearParidadYTipoCambio";

CREATE PROCEDURE
--  Proposito: crear instrumento
--  Autor: - TEAM ORDENES 
--  Fecha Creacion: 20/07/2015 
 
"BSCL.Plataforma.BD.Servicios::MER.CrearParidadYTipoCambio" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.

	IN p_Registros			TABLE(
								"IdInterno"				BIGINT,
								"Fecha"					NVARCHAR(10),
								"IdMoneda"				NVARCHAR(3),
								"ParidadMonedaCambio"	DECIMAL(26,8),
								"IdTipoRegistro"		NVARCHAR(40)							
		
	),
    
    OUT p_out_Resultado		TABLE(
    							"IdInterno"				BIGINT
    ),						
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	p_out_AdvertenciasContador = 0;
	p_out_ErroresContador = 0;
		
	--validacion sin moneda
	registro_sin_moneda = select "IdInterno" 
						from :p_Registros reg 
						where COALESCE("IdMoneda",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'REGISTRO_SIN_MONEDA' AS "Codigo"
							from :registro_sin_moneda;
	---------------------------------------------------------------------------------------		
	--validacion sin origen
	registro_sin_origen = select "IdInterno" 
						from :p_Registros reg 
						where COALESCE("IdTipoRegistro",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'REGISTRO_SIN_ORIGEN' AS "Codigo"
							from :registro_sin_moneda
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------	
	---------------------------------------------------------------------------------------		
	--validacion sin fecha
	registro_sin_fecha = select "IdInterno" 
						from :p_Registros reg 
						where COALESCE("Fecha",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'REGISTRO_SIN_FECHA' AS "Codigo"
							from :registro_sin_fecha
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------		
	--validacion registro ya existe
	registro_ya_existe = select "IdInterno" 
						from :p_Registros reg 
						INNER JOIN "BSCL.Plataforma.BD::MER.ParidadYTipoCambio" par on reg."Fecha" = par."Fecha" and reg."IdTipoRegistro" = par."IdTipoRegistro" and par."IdMoneda" = reg."IdMoneda" 
						AND par."IdEmpresa"= :p_IdEmpresa
						;
	
	v_out_ErroresDetalle = select "IdInterno", 'REGISTRO_YA_EXISTE' AS "Codigo"
							from :registro_ya_existe
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------					
	v_registros_ok =	select "IdInterno" 
				from :p_Registros reg
				except
				select "IdInterno" from :registro_sin_moneda
				except
				select "IdInterno" from :registro_sin_origen
				except
				select "IdInterno" from :registro_sin_fecha
				except
				select "IdInterno" from :registro_ya_existe;
				
	v_registros = SELECT reg."IdInterno",
							"Fecha","IdMoneda",p_IdUsuario as "IdUsuarioIngreso",
							"ParidadMonedaCambio",'ACT' as "IdEstado","IdTipoRegistro"
							FROM :p_Registros reg
							INNER JOIN :v_registros_ok ok on ok."IdInterno" = reg."IdInterno";
	---------------------------------------------------------------------------------------
	INSERT INTO "BSCL.Plataforma.BD::MER.ParidadYTipoCambio" ("IdEmpresa","Fecha","IdMoneda","IdUsuarioUltimaModificacion",
	"ParidadMonedaCambio","IdEstado","IdTipoRegistro","FechaUltimaModificacion")
	SELECT :p_IdEmpresa,"Fecha","IdMoneda",:p_IdUsuario,
	"ParidadMonedaCambio","IdEstado","IdTipoRegistro",CURRENT_DATE
	FROM :v_registros;
	
	v_tablaMensajes = select "IdInterno","Codigo",'' as "CodigoDato1", '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from :v_out_ErroresDetalle;
	
	CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_tablaMensajes,v_out_TablaMensajes);
	
	p_out_ErroresDetalle = select det."IdInterno",det."Codigo",outt."Descripcion" from :v_out_ErroresDetalle det INNER JOIN :v_out_TablaMensajes outt on outt."IdInterno" = det."IdInterno" and outt."Codigo" = det."Codigo";
	select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
	
	p_out_Resultado = select "IdInterno" from :v_registros;
	
	
	
END;
