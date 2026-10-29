SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.NotificarVariacionCapital";
-- 
--  Proposito: Servicio para la notificacion de variaciones en eventos de capital
--  Autor: - NVILLANUEVA
--  Fecha Creacion: 19-01-2018
--
GO  
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.NotificarVariacionCapital" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca
	--IN  p_IdPortafolio				bigint,

	-- Parametros de entrada para la creacion del usuario
	IN  p_IdEventoCapital		BIGINT--p.eventocapital

) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN


	--------------------
	
		
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_IdPersonaRemitente	      BIGINT;
		DECLARE v_IdDireccionPersonaRemitente BIGINT;
		DECLARE v_FechaSistema                NVARCHAR(10);
		DECLARE v_hora                        NVARCHAR(10);
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			
		END;
		--------------------
		--Negocio Aqui
		-------------------
	   SELECT CURRENT_TIME into v_hora from DUMMY;
	   CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	   
		/***/
	    -- remitente
			SELECT CE."IdPersona", CE."IdDireccionElectronica"  INTO v_IdPersonaRemitente, v_IdDireccionPersonaRemitente
			FROM "BSCL.Plataforma.BD::ACC.Usuario" U
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad"
			WHERE "IdUsuario" = :p_IdUsuario;
		
			
			t_cliente = 
                SELECT 
                    --cabecera
                    ECC."IdEventoCapitalCliente" 													AS "IdSecuencia",
                    CCL."NombreCompleto" 															AS "Nombre",
                    ECC."DocumentoIdentidad" 														AS "SocNeg",
                    TO_NVARCHAR(ECC."IdPortafolio") 												AS "Port",
                    ECC."IdPortafolio",
                    COALESCE(CPT."Numero", '') 														AS "Fono",
                    COALESCE(CPDE."Direccion", '') 													AS "Mail",
                    CEMP."NombreCompleto" 															AS "Agente",
                    TO_VARCHAR(TO_DATE(v_FechaSistema, 'YYYY-MM-DD'), 'DD-MM-YYYY') 				AS "Fecha",
                    v_hora 																			AS "Hora",
                    --detalle                
                    TEC."Nombre" 																	AS "Tipo",
                    ECC."Nemo" 																		AS "Familia",
                    "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
                    CASE ECC."IdTipoEventoCapital"
                        WHEN 'CC'   THEN    CAST(ROUND(IFNULL(ECC."MontoCapital",0)) AS NVARCHAR)
                        WHEN 'DA'   THEN    CAST(IFNULL(EC."Factor",0) AS NVARCHAR)
                        WHEN 'DA'   THEN    CAST(IFNULL(EC."Factor",0) AS NVARCHAR)
                        ELSE        CAST(IFNULL(EC."Factor",0) AS NVARCHAR)
                    END
                    ,CASE WHEN ECC."IdMoneda"='CLP' THEN 'i' ELSE 'd' END,CASE WHEN ECC."IdMoneda"='CLP' THEN 0 ELSE 3 END ) AS "Pago",
                    CASE ECC."IdTipoEventoCapital"
                        WHEN 'DV' THEN IFNULL(ECC."IdMoneda",'No existe información')
                        WHEN 'CC' THEN IFNULL(ECC."IdMoneda",'No existe información')
                        WHEN 'DA' THEN 'Unidades'
                        WHEN 'DS' THEN 'Unidades'
                        WHEN 'CA' THEN 'Unidades'                                                                       
                        WHEN 'EP' THEN 'Unidades'
                        ELSE 'Falta'                                                
                    END 																			AS "PagoMo",
                    "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
                    CASE ECC."IdTipoEventoCapital"
                        WHEN 'CC'   THEN    CAST(ROUND(IFNULL(ECC."MontoInteres",0)) AS NVARCHAR)
                        WHEN 'DA'   THEN    CAST(ROUND(ECC."CantidadNueva") AS NVARCHAR)
                        WHEN 'EP'   THEN    CAST(ROUND(ECC."CantidadNueva") AS NVARCHAR)                                    
                        ELSE                CAST(ROUND(ECC."Total") AS NVARCHAR)
                    END
                    ,CASE WHEN ECC."IdMoneda"='CLP' THEN 'i' ELSE 'd' END,CASE WHEN ECC."IdMoneda"='CLP' THEN 0 ELSE 3 END ) AS "Monto",
                    CASE ECC."IdTipoEventoCapital"
                        WHEN 'DV' THEN IFNULL(ECC."IdMoneda",'No existe información')
                        WHEN 'CC' THEN IFNULL(ECC."IdMoneda",'No existe información')
                        WHEN 'DA' THEN 'Unidades'
                        WHEN 'DS' THEN 'Unidades'
                        WHEN 'CA' THEN 'Unidades'                                   
                        WHEN 'EP' THEN 'Unidades'
                        ELSE 'Falta'                                                
                    END 																			AS "MontoMo",
                    ECC."IdTipoEventoCapital"                                                       AS "TipoEventoCapital",
                    TO_VARCHAR(TO_DATE(ECC."FechaPago", 'YYYY-MM-DD'), 'DD-MM-YYYY') 				AS "FechaPago",
                    ES."Descripcion" 																AS "Estado",
                    --pie
                    CCL."IdDireccionElectronicaPrincipal",
                    CCL."IdPersona",
                    CCDG."RazonSocial"																AS "Corredora",
                    CCDG."Telefono"                                                                 AS "AtencionCliente"
                FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECC
                INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EC ON EC."IdEventoCapital" = ECC."IdEventoCapital"
                INNER JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" TEC ON ECC."IdTipoEventoCapital" = TEC."IdTipoEventoCapital"
                INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON (ECC."IdPortafolio" = PORT."IdPortafolio")
                INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL ON CCL."IdCliente" = PORT."IdCliente"
                LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CCL."IdTelefonoPrincipal" = CPT."IdTelefono"
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CCL."IdDireccionElectronicaPrincipal"
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON CCL."IdEmpleadoEjecutivo" = CEMP."IdEmpleado"
                LEFT JOIN "BSCL.Custodia.BD::CUS.EstadoCustodia" ES ON ECC."IdEstado" = ES."IdEstado" AND ES."NombreEntidad" = 'CUSTODIA.EVENTO'
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :p_IdEmpresa
                WHERE EC."IdEventoCapital" = :p_IdEventoCapital;
				
			--construir notificacion
			p_Eventos = SELECT 
							'CARTAVAR_CAP'			AS "CodigoTipoEvento", 
							"IdSecuencia", 
							CURRENT_UTCTIMESTAMP	AS "FechaCreacionSistema", 
							:v_IdPersonaRemitente	AS "IdPersonaRemitente", 
							"IdPersona"				AS "IdPersonaDestinatario",
							"IdPortafolio"
						FROM :t_cliente;
			
	    	p_IdDirecciones = 	SELECT 
									E."IdSecuencia",
									C."IdDireccionElectronicaPrincipal"	AS "IdDireccion",
									'TO'								AS "TipoDestinatario"
								FROM :p_Eventos E
									INNER JOIN :t_cliente C 
										ON E."IdSecuencia" = C."IdSecuencia"
								UNION ALL
								SELECT 
									E."IdSecuencia",
									v_IdDireccionPersonaRemitente	AS "IdDireccion",
									'CC'							AS "TipoDestinatario"
								FROM :p_Eventos E;
			
			p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
			
			
			p_EventoDetalle = 	SELECT "IdSecuencia", "Llave", "Valor"
								FROM (
									SELECT * ,
									        
									COALESCE(
									"1" || "IdSecuencia",
                                    "2" || "Nombre",
                                    "3" || "SocNeg",
                                    "4" || "Port",
                                    "5" || "Fono",
                                    "6" || "Mail",
                                    "7" || "Agente",
                                    "8" || "Fecha",
                                    "9" || "Hora",
                                    "10" || "Tipo",
                                    "11" || "Familia",
                                    "12" || "Pago",
                                    "13" || "PagoMo",
                                    "14" || "Monto",
                                    "15" || "MontoMo",
                                    "16" || "FechaPago",
                                    "17" || "Estado",
                                    "18" || "IdDireccionElectronicaPrincipal",
                                    "19" || "IdPersona",
                                    "20" || "Corredora",
                                    "21" || "AtencionCliente",
                                    "22" || "TipoEventoCapital"
									) AS "Valor",
									        
									COALESCE(
									"1" || 'IDSECUENCIA', 
									"2" || 'CLIENTE', 
									"3" || 'DOCUMENTOIDENTIDAD', 
									"4" || 'PORTAFOLIO', 
									"5" || 'TELEFONO', 
									"6" || 'CORREO', 
									"7" || 'AGENTE', 
									"8" || 'FECHA', 
									"9" || 'HORA', 
									"10" || 'TIPO', 
									"11" || 'FAMILIA', 
									"12" || 'PAGO', 
									"13" || 'PAGOMO', 
									"14" || 'MONTO', 
									"15" || 'MONTOMO', 
									"16" || 'FECHAPAGO', 
									"17" || 'ESTADO', 
									"18" || 'IDDIRECCIONELECTRONICAPRINCIPAL', 
									"19" || 'IDPERSONA', 
									"20" || 'CORREDORA',
									"21" || 'ATENCIONCLIENTE',
									"22" || 'TIPOEVENTOCAPITAL'
									) AS "Llave" 
									FROM :t_cliente T, "BSCL.Plataforma.BD::PLA.Identidad"
								)
								WHERE "Valor" IS NOT NULL;
								
--			SELECT 'p_Eventos',* FROM :p_Eventos;
--			SELECT 'p_EventoDetalle',* FROM :p_EventoDetalle;
--			SELECT 'p_IdDirecciones',* FROM :p_IdDirecciones;
			--Guardar eventos
			CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
				p_IdEmpresa 		=> p_IdEmpresa,
				p_IdUsuario 		=> p_IdUsuario,
				p_IdModulo 			=> p_IdModulo,
				p_Eventos 			=> :p_Eventos,
				p_Valores 			=> :p_EventoDetalle,
				p_ArchivosAdjuntos	=> :p_ArchivosAdjuntos,
				p_IdDirecciones 	=> :p_IdDirecciones,
				p_out_IdEvento 		=> p_out_IdEvento
			);
	END;