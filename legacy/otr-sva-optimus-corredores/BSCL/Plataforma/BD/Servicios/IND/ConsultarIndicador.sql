SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::IND.ConsultarIndicador";
GO
CREATE PROCEDURE 
--
--  Proposito: Devuelve preferencias de la entidad especificada
--  Autor: - CFLORES
--  Fecha Creacion: 11/04/2016
--
"BSCL.Plataforma.BD.Servicios::IND.ConsultarIndicador" (

IN p_IdEmpresa				BIGINT,
IN p_IdEntidad				BIGINT			DEFAULT 0,
IN p_IdTipoEntidad			NVARCHAR(40)	DEFAULT '', ---CLIENTE/PORTAFOLIO/EMPLEADO
IN p_CodigoTipoIndicador  	NVARCHAR(10)	DEFAULT '',
IN p_GrupoIndicador       	NVARCHAR(40) 	DEFAULT '',
IN p_ModeloCliente			NVARCHAR(40)	DEFAULT '', ---<TipoCliente>
IN p_IdIndicador			BIGINT			DEFAULT 0,

OUT p_out_IndicadoresContador BIGINT,
OUT p_out_Indicadores table ("IdIndicador"    		BIGINT
							,"IdTipoIndicador" 		BIGINT
							,"NombreTipoIndicador" 	NVARCHAR(255)
							,"ClaseDato"			NVARCHAR(40)
							,"CodigoTipoIndicador" 	NVARCHAR(40)
							,"ValorIndicador"		NVARCHAR(255)
							,"IdEstadoAutorizacion"	NVARCHAR(3)
							,"GrupoIndicador"		NVARCHAR(40)
							,"IdUso"				NVARCHAR(3)
						   	,"CantidadDecimales"	BIGINT
						    ,"LongitudMinima"		BIGINT
						  	,"LongitudMaxima"		BIGINT
						  	,"IdTipoEntidad" 		NVARCHAR(40)
							,"IdEntidad"			BIGINT
							,"ModeloCliente"		NVARCHAR(40)
						  	,"IdTipoPersona"		NVARCHAR(10)
						   ),
OUT	p_out_GrupoIndicadoresContador BIGINT,
OUT p_out_GrupoIndicadores TABLE ("GrupoIndicador" NVARCHAR(40)),
OUT p_out_ErroresDetalle   TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
						   )
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS

BEGIN
	DECLARE SALIDA_CONTROLADA_NO_MODELO_CLIENTE	CONDITION;
	DECLARE v_IdUso				NVARCHAR(3):='IND'; --Se buscan solo indicadores para la Logica de Negocio
	DECLARE v_IdEntidadBase		BIGINT:=0;
	DECLARE v_IdTipoCliente	 	NVARCHAR(40):='';
	DECLARE V_IdIndicadorCero NVARCHAR(1):='N';	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_MODELO_CLIENTE
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'MODELO_CLIENTE_ENTRADA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo","Descripcion" FROM :v_erroroutmsg;
	END;
	
	IF 	p_ModeloCliente='' THEN
	
		IF p_IdEntidad = 0 THEN
			SIGNAL SALIDA_CONTROLADA_NO_MODELO_CLIENTE;
		END IF;
	
		IF p_IdTipoEntidad='CLIENTE' AND p_IdEntidad<>0 THEN
			p_out_GrupoIndicadores =	SELECT IND."GrupoIndicador" FROM
			"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
				WHERE  IND."IdUso" = v_IdUso
						AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
						AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
						AND (p_IdEntidad = 0 OR IND."IdCliente"=p_IdEntidad  )
						AND (p_IdEmpresa = IND."IdEmpresa" )
			GROUP BY IND."GrupoIndicador";

			p_out_Indicadores =
					SELECT IND."IdIndicador",
						IND."IdTipoIndicador",
						IND."NombreTipoIndicador",
						IND."ClaseDato",
						IND."CodigoTipoIndicador",
						IND."ValorIndicador",
						'ING' AS "IdEstadoAutorizacion",
						IND."GrupoIndicador",
						IND."IdUso",
						IND."CantidadDecimales",
						IND."LongitudMinima",
						IND."LongitudMaxima",
						IND."IdTipoEntidad",
						IND."IdCliente" AS "IdEntidad",
						IND."ModeloCliente",
					    IND."IdTipoPersona"
					FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
								WHERE  IND."IdUso" = v_IdUso
										AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
										AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
										AND (p_IdEntidad = 0 OR IND."IdCliente"=p_IdEntidad  )
										AND (p_IdEmpresa = IND."IdEmpresa")
										order by "IdTipoIndicador"	;
		END IF;
		IF p_IdTipoEntidad='PORTAFOLIO' AND p_IdEntidad<>0 THEN
			p_out_GrupoIndicadores =	SELECT IND."GrupoIndicador" FROM
			"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND
				WHERE  IND."IdUso" = v_IdUso
						AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
						AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
						AND (p_IdEntidad = 0 OR IND."IdPortafolio"=p_IdEntidad  )
						AND (p_IdEmpresa = IND."IdEmpresa")
			GROUP BY IND."GrupoIndicador";

			p_out_Indicadores =
					SELECT IND."IdIndicador",
						IND."IdTipoIndicador",
						IND."NombreTipoIndicador",
						IND."ClaseDato",
						IND."CodigoTipoIndicador",
						IND."ValorIndicador",
						'ING' AS "IdEstadoAutorizacion",
						IND."GrupoIndicador",
						IND."IdUso",
						IND."CantidadDecimales",
						IND."LongitudMinima",
						IND."LongitudMaxima",
						IND."IdTipoEntidad",
						IND."IdPortafolio" AS "IdEntidad",
						IND."ModeloCliente",
						--'' AS "IdTipoPersona"
					    IND."IdTipoPersona"
					FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"IND
								WHERE  IND."IdUso" = v_IdUso
										AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
										AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
										AND (p_IdEntidad = 0 OR IND."IdPortafolio"=p_IdEntidad  )
										AND (p_IdEmpresa = IND."IdEmpresa");
		END IF;
		IF p_IdTipoEntidad='PERSONA' AND p_IdEntidad<>0 THEN
			p_out_GrupoIndicadores =	SELECT IND."GrupoIndicador" FROM
			"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND
				WHERE  IND."IdUso" = v_IdUso
						AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
						AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
						AND (p_IdEntidad = 0 OR IND."IdPersona"=p_IdEntidad  )
						AND (p_IdEmpresa = IND."IdEmpresa")
			GROUP BY IND."GrupoIndicador";

			p_out_Indicadores =
					SELECT IND."IdIndicador",
						IND."IdTipoIndicador",
						IND."NombreTipoIndicador",
						IND."ClaseDato",
						IND."CodigoTipoIndicador",
						IND."ValorIndicador",
						'ING' AS "IdEstadoAutorizacion",
						IND."GrupoIndicador",
						IND."IdUso",
						IND."CantidadDecimales",
						IND."LongitudMinima",
						IND."LongitudMaxima",
						IND."IdTipoEntidad",
						IND."IdPersona" AS "IdEntidad",
						'' as "ModeloCliente",
					    IND."IdTipoPersona"
					FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND
								WHERE  IND."IdUso" = v_IdUso
										AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
										AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
										AND (p_IdEntidad = 0 OR IND."IdPersona"=p_IdEntidad  )
										AND (p_IdEmpresa = IND."IdEmpresa")
										order by "IdTipoIndicador"	;
		END IF;
		
		IF p_IdTipoEntidad='EMPLEADO' AND p_IdEntidad<>0 THEN
			p_out_GrupoIndicadores =	SELECT IND."GrupoIndicador" FROM
			"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" IND
				WHERE  IND."IdUso" = v_IdUso
						AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
						AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
						AND (p_IdEntidad = 0 OR IND."IdEmpleado"=p_IdEntidad  )
						AND (p_IdEmpresa = IND."IdEmpresa")
			GROUP BY IND."GrupoIndicador";

			p_out_Indicadores =
					SELECT IND."IdIndicador",
						IND."IdTipoIndicador",
						IND."NombreTipoIndicador",
						IND."ClaseDato",
						IND."CodigoTipoIndicador",
						IND."ValorIndicador",
						'ING' AS "IdEstadoAutorizacion",
						IND."GrupoIndicador",
						IND."IdUso",
						IND."CantidadDecimales",
						IND."LongitudMinima",
						IND."LongitudMaxima",
						IND."IdTipoEntidad",
						IND."IdEmpleado" AS "IdEntidad",
						'' as "ModeloCliente",
					    IND."IdTipoPersona"
					FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" IND
								WHERE  IND."IdUso" = v_IdUso
										AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
										AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador" = p_GrupoIndicador)
										AND (p_IdEntidad = 0 OR IND."IdEmpleado" = p_IdEntidad  )
										AND (p_IdEmpresa = IND."IdEmpresa")
										order by "IdTipoIndicador"	;
		END IF;


	ELSE
	
	p_out_GrupoIndicadores =	SELECT IND."GrupoIndicador" FROM
			"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo" IND
				WHERE  IND."IdUso" = v_IdUso
						AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
						AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
						AND (IND."IdTipoCliente"=p_ModeloCliente  )
						AND (p_IdTipoEntidad='' OR IND."IdTipoEntidad" =  p_IdTipoEntidad)
						AND (p_IdEmpresa = IND."IdEmpresa")
			GROUP BY IND."GrupoIndicador";

			p_out_Indicadores =
					SELECT IND."IdIndicador",
						IND."IdTipoIndicador",
						IND."NombreTipoIndicador",
						IND."ClaseDato",
						IND."CodigoTipoIndicador",
						IND."ValorIndicador",
						'ING' AS "IdEstadoAutorizacion",
						IND."GrupoIndicador",
						IND."IdUso",
						IND."CantidadDecimales",
						IND."LongitudMinima",
						IND."LongitudMaxima",
						IND."IdTipoEntidad",
						0 AS "IdEntidad",
						IND."ModeloCliente",
					    IND."IdTipoPersona"
					FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo" IND
								WHERE  (IND."IdUso" = v_IdUso OR IND."CodigoTipoIndicador" = 'NPA')
										AND (p_CodigoTipoIndicador = '' OR  IND."CodigoTipoIndicador"= p_CodigoTipoIndicador)
										AND (p_GrupoIndicador = '' OR  IND."GrupoIndicador"= p_GrupoIndicador)
										AND (IND."IdTipoCliente"=p_ModeloCliente  )
										AND (IND."IdTipoEntidad" = p_IdTipoEntidad )
										AND (p_IdEmpresa = IND."IdEmpresa")
										order by "IdTipoIndicador";



	END IF;

	select count(1) into p_out_GrupoIndicadoresContador from :p_out_GrupoIndicadores;
	select count(1) into p_out_IndicadoresContador from :p_out_Indicadores;

END;
