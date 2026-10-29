SET SCHEMA "BSCL"; 

drop table "BSCL.Plataforma.BD::VAL.Cupon_Entrada_ListaInstrumentos";
drop table "BSCL.Plataforma.BD::VAL.Cupon_Salida_ListaCupones";

CREATE GLOBAL TEMPORARY TABLE "BSCL.Plataforma.BD::VAL.Cupon_Entrada_ListaInstrumentos"(
	-- DATOS TAREA
	"IdTarea" 				BIGINT,      				-- Id Tarea Negocio las pide de "BSCL.Plataforma.BD.Secuencias::INS.TareaValorizacion".NEXTVAL				 	
	-- DATOS ENTRADA CUPON
	"Nemo"					NVARCHAR(100),				-- E|S 	- Nemotécnico
	"FechaIni"				DATE,						-- E|S 	- Pendiente documento definicion
	"FechaFin"				DATE,						-- E|S 	- Pendiente documento definicion
	"CodTipoMod"			NVARCHAR(1),				-- E| 	- Pendiente documento definicion
	"Plazo"					BIGINT						-- E|S 	- Pendiente documento definicion		
)ON COMMIT DELETE ROWS;

CREATE CPBTREE INDEX "INX01_BSCL.Plataforma.BD::VAL.Cupon_Entrada_ListaInstrumentos" 
ON "BSCL.Plataforma.BD::VAL.Cupon_Entrada_ListaInstrumentos"(
	"IdTarea" ASC
);

CREATE GLOBAL TEMPORARY TABLE "BSCL.Plataforma.BD::VAL.Cupon_Salida_ListaCupones"(
	-- DATOS TAREA
	"IdTarea" 				BIGINT,
	"GrupoValorizacion"		BIGINT,	 
	"FechaSolicitud" 		SECONDDATE,
 	"IdEstado" 				NVARCHAR(3),
	 -- DATOS ENTRADA|SALIDA
	"Nemo"					NVARCHAR(100),				-- E|S 	- Nemotécnico
	"FechaIni"				DATE,						-- E|S 	- Pendiente documento definicion
	"FechaFin"				DATE,						-- E|S 	- Pendiente documento definicion
	"CodTipoMod"			NVARCHAR(1),				-- E| 	- Pendiente documento definicion
	"Plazo"					BIGINT,						-- E|S 	- Pendiente documento definicion		
	"Glosa"					NVARCHAR(100),				--  |S	- Pendiente documento definicion		
	"CodigoEmisor"			NVARCHAR(40),				--  |S 	- Pendiente documento definicion
	"FechaEmision"			DATE,						--  |S 	- Pendiente documento definicion
	"FechaVencimiento"		DATE,						--  |S 	- Pendiente documento definicion
	"TipoMod"				NVARCHAR(40),				--  |S 	- Pendiente documento definicion
	"TipoUnidad"			NVARCHAR(40),				--  |S 	- Pendiente documento definicion -- equivale a tipo moneda en valoriz.
	"ValorReferencial"		NVARCHAR(100),				--  |S 	- Pendiente documento definicion
	"CuponAmortizacion"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"CuponFactorAjuste"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"CuponFechaEfectiva"	DATE,						--  |S 	- Pendiente documento definicion
	"CuponFechaHabil"		DATE,						--  |S 	- Pendiente documento definicion
	"CuponFlujo"			DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"CuponIntereses"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"CuponSecuencia"		BIGINT,						--  |S 	- Pendiente documento definicion
	-- salida invocacion webservice desde el job
	"WsCallerCodSalida"		NVARCHAR(100),				--Salida caller java consulta WS
	"WsCallerDescSalida"	NVARCHAR(100)				--Salida caller java consulta WS
)ON COMMIT DELETE ROWS;

CREATE CPBTREE INDEX "INX01_BSCL.Plataforma.BD::VAL.Cupon_Salida_ListaCupones" 
ON "BSCL.Plataforma.BD::VAL.Cupon_Salida_ListaCupones"(
	"IdTarea" ASC
);

commit;