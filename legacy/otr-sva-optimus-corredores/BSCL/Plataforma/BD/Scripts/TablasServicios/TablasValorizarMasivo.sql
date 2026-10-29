SET SCHEMA "BSCL"; 

drop table "BSCL.Plataforma.BD::VAL.ValorizarMasivo_Entrada_Instrumentos";
drop table "BSCL.Plataforma.BD::VAL.ValorizarMasivo_Salida_InstrumentosValorizados";
drop table "BSCL.Plataforma.BD::PLA.ValorizarMasivo_Proceso_ValVALJOBS";--TIP_VALJOBS

CREATE GLOBAL TEMPORARY TABLE "BSCL.Plataforma.BD::VAL.ValorizarMasivo_Entrada_Instrumentos"(
	 -------------------------------------
	 -- DATOS TAREA
	 "IdTarea" 				BIGINT,      				-- Id Tarea Negocio las pide de "BSCL.Plataforma.BD.Secuencias::INS.TareaValorizacion".NEXTVAL				 	
	 "TipoValorizacion"		NVARCHAR(4), 				-- Tipo de valorizacion a usar x valorizador  -- TODO: BORRAR! NO ES NECESARIA! AL MENOS QUE SE IMPLEMENTE CAPA DE VALIDACION.
	 -------------------------------------
	 -- DATOS ENTRADA - Los requeridos son de acuerdo al tipo de TipoValorizacion 
	"TipoVal" 				BIGINT,						-- E|S	- Tipo de valorización
	"Nemo"					NVARCHAR(100),				-- E|S 	- Nemotécnico
	"FechaVal"				DATE,						-- E|S 	- Fecha de Valorización (AAAAMMDD)
	"FechaEmi"				DATE,						-- E|S 	- Fecha de Emisión del instrumento (AAAAMMDD)
	"FechaVcto"				DATE,						-- E|S 	- Fecha de Vencimiento del instrumento (AAAAMMDD)
	"NumeroDias"			BIGINT,						-- E|S 	- N° Días al Vencimiento del instrumento
	"TasaTir"				DECIMAL(26,8),				-- E|S 	- Single Tasa Interna Retorno, Real Anual (IIF, IRF) Single => BIGDECIMAL
	"TasaTip"				DECIMAL(26,8),				-- E|S 	- Single Tasa Flotante Estimada (IIF, IRF)
	"TasaInteres"			DECIMAL(26,8),				-- E|S 	- Single Tasa Interés (IIF)
	"ValRescateP"			DECIMAL(26,8),				-- E|S 	- Valor Rescate Pesos a fecha valorización
	"ValRescateR"			DECIMAL(26,8),				-- E|S 	- Valor Rescate en U.F. a fecha valorización
	"ValCaptacionP"			DECIMAL(26,8),				-- E|S 	- Valor Captación Pesos a fecha valorización
	"ValCaptacionR"			DECIMAL(26,8),				-- E|S 	- Valor Captación U.F. a fecha valorización
	"GlosaEmisor"			NVARCHAR(100),				-- E|S 	- Descripción Emisor
	"GlosaMensaje"			NVARCHAR(100),				-- E|S 	- Glosa mensaje valorización
	"Cantidad"				DECIMAL(26,8),				-- E|S 	- Cantidad (unidades)
	"UnidadMon"				NVARCHAR(40),				-- E|S 	- Descripción Unidad Monetaria
	"Precio"				DECIMAL(26,8),				-- E|S 	- Precio del instrumento
	"MontoPeso"				DECIMAL(26,8),				-- E|S 	- Monto en Pesos a fecha valorización
	"MontoUF"				DECIMAL(26,8),				-- E|S 	- Monto en U.F. a fecha valorización
	"MontoUS"	 			DECIMAL(26,8),				-- E|S 	- Monto en US$ a fecha valorización
	"AnoVcto"				BIGINT,						-- E|S 	- Año vencimiento del instrumento
	"MesVcto"				BIGINT,						-- E|S 	- Mes vencimiento del instrumento
	"Duration"				DECIMAL(26,8),				-- E|S 	- Valor Duration Macaulay
	"ValorBase"				DECIMAL(26,8),				-- E|S 	- Valor Base
	"Convexidad"			DECIMAL(26,8),				-- E|S 	- Valor Convexividad
	"ValorPar2"				DECIMAL(26,8),				-- E|S 	- Valor Par en Pesos
	"ValorPar"				DECIMAL(26,8),				-- E|S 	- Valor Par
	"TipoTasa"				NVARCHAR(40),				-- E|S 	- Descripción Tipo Tasa
	"CalculoEst"			NVARCHAR(40),				-- E|S 	- Índice Cálculo Estimado
	"APlazo"				BIGINT,						-- E|S 	- Plazo en años del instrumento
	"ValorRM"				DECIMAL(26,8),				-- E|S 	- Valor Inflación Promedio Anual
	"ValorNom"				DECIMAL(26,8),				-- E|S 	- Valor Nominal
	"TipoMoneda"			BIGINT,						-- E|S 	- Código Tipo Moneda: 1=UF, 2=US$
	"CLiq"					NVARCHAR(40),				-- E|  	- Condición de Liquidación (PH, PM, CN) 	
	PRIMARY KEY ("IdTarea")
)ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE "BSCL.Plataforma.BD::VAL.ValorizarMasivo_Salida_InstrumentosValorizados"(
	--- DATOS TAREA
	"IdTarea" 				BIGINT 			,
	"GrupoValorizacion"		BIGINT			,	 
	"FechaSolicitud" 		SECONDDATE		,
 	"IdEstado" 				NVARCHAR(3) 	,
 	-- DATOS VALORIZADOR
	"RutinaValorizacion" 	NVARCHAR(4) 	, ----BCS  
	"OpcionValorizacion" 	NVARCHAR(4) 	, -- COLUMNA LEGACY --Las mismas que el valorizador  
	"TipoValorizacion"		NVARCHAR(4) 	, -- COLUMNA LEGACY --Tipo de valorizacion a usar x valorizador 
	 -- DATOS ENTRADA|SALIDA
	"TipoVal" 				BIGINT,						-- E|S	- Tipo de valorización
	"Nemo"					NVARCHAR(100),				-- E|S 	- Nemotécnico
	"FechaVal"				DATE,						-- E|S 	- Fecha de Valorización (AAAAMMDD)
	"FechaEmi"				DATE,						-- E|S 	- Fecha de Emisión del instrumento (AAAAMMDD)
	"FechaVcto"				DATE,						-- E|S 	- Fecha de Vencimiento del instrumento (AAAAMMDD)
	"NumeroDias"			BIGINT,						-- E|S 	- N° Días al Vencimiento del instrumento
	"TasaTir"				DECIMAL(26,8),				-- E|S 	- Single Tasa Interna Retorno, Real Anual (IIF, IRF) Single => BIGDECIMAL
	"TasaTip"				DECIMAL(26,8),				-- E|S 	- Single Tasa Flotante Estimada (IIF, IRF)
	"TasaInteres"			DECIMAL(26,8),				-- E|S 	- Single Tasa Interés (IIF)
	"ValRescateP"			DECIMAL(26,8),				-- E|S 	- Valor Rescate Pesos a fecha valorización
	"ValRescateR"			DECIMAL(26,8),				-- E|S 	- Valor Rescate en U.F. a fecha valorización
	"ValCaptacionP"			DECIMAL(26,8),				-- E|S 	- Valor Captación Pesos a fecha valorización
	"ValCaptacionR"			DECIMAL(26,8),				-- E|S 	- Valor Captación U.F. a fecha valorización
	"GlosaEmisor"			NVARCHAR(100),				-- E|S 	- Descripción Emisor
	"GlosaMensaje"			NVARCHAR(100),				-- E|S 	- Glosa mensaje valorización
	"Cantidad"				DECIMAL(26,8),				-- E|S 	- Cantidad (unidades)
	"UnidadMon"				NVARCHAR(40),				-- E|S 	- Descripción Unidad Monetaria
	"Precio"				DECIMAL(26,8),				-- E|S 	- Precio del instrumento
	"MontoPeso"				DECIMAL(26,8),				-- E|S 	- Monto en Pesos a fecha valorización
	"MontoUF"				DECIMAL(26,8),				-- E|S 	- Monto en U.F. a fecha valorización
	"MontoUS"	 			DECIMAL(26,8),				-- E|S 	- Monto en US$ a fecha valorización
	"AnoVcto"				BIGINT,						-- E|S 	- Año vencimiento del instrumento
	"MesVcto"				BIGINT,						-- E|S 	- Mes vencimiento del instrumento
	"Duration"				DECIMAL(26,8),				-- E|S 	- Valor Duration Macaulay
	"ValorBase"				DECIMAL(26,8),				-- E|S 	- Valor Base
	"Convexidad"			DECIMAL(26,8),				-- E|S 	- Valor Convexividad
	"ValorPar2"				DECIMAL(26,8),				-- E|S 	- Valor Par en Pesos
	"ValorPar"				DECIMAL(26,8),				-- E|S 	- Valor Par
	"TipoTasa"				NVARCHAR(40),				-- E|S 	- Descripción Tipo Tasa
	"CalculoEst"			NVARCHAR(40),				-- E|S 	- Índice Cálculo Estimado
	"APlazo"				BIGINT,						-- E|S 	- Plazo en años del instrumento
	"ValorRM"				DECIMAL(26,8),				-- E|S 	- Valor Inflación Promedio Anual
	"ValorNom"				DECIMAL(26,8),				-- E|S 	- Valor Nominal
	"TipoMoneda"			BIGINT,						-- E|S 	- Código Tipo Moneda: 1=UF, 2=US$
	"CLiq"					NVARCHAR(40),				-- E|  	- Condición de Liquidación (PH, PM, CN)
	"Gestion"				NVARCHAR(1),				-- E|  	- Pendiente de inyectar 
	"Estado"				NVARCHAR(40),				--  |S 	- Resultado de la consulta (0, 1, 2)
	"Glosa"					NVARCHAR(100),				--  |S 	- Descripcion
	"Hora"					NVARCHAR(40),				--  |S 	- FechaHora de la información (AAAAMMDDHHMMSSCC)
	"WsCallerCodSalida"		NVARCHAR(100),	--Salida caller java consulta WS
	"WsCallerDescSalida"	NVARCHAR(100),	--Salida caller java consulta WS
PRIMARY KEY ("IdTarea")
)ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE "BSCL.Plataforma.BD::PLA.ValorizarMasivo_Proceso_ValVALJOBS"(
	GRUPO  		bigint,
	PROGRAMA  	bigint,
	TAREA 		bigint,
	ORDEN 		bigint,
	OPCION 		nvarchar(4),
	TIPOUNIDAD 	int			
)ON COMMIT DELETE ROWS;

commit;
 

 

 

 





 






 

 
 

