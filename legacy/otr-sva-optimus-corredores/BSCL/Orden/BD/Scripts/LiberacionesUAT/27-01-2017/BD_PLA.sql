set schema "BSCL";

ALTER TABLE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" add ("NumeroCupones" 						INT NOT NULL DEFAULT 0);
CREATE UNIQUE INDEX idxBolsaTipoInstrumento ON "BSCL"."BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"("IdTipoInstrumento", "IdBolsa");

CREATE COLUMN TABLE "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" (
	"IdParametroFinanciero" 		NVARCHAR(40)	NOT NULL,	 
	"Periodo"						NVARCHAR(2)		NOT NULL,
	"Nombre" 						NVARCHAR(100) 	NOT NULL DEFAULT '',
	"Descripcion"					NVARCHAR(500)	NOT NULL DEFAULT '',
	"IdMonedaParametro"				NVARCHAR(3)		NULL,
	"IdMonedaValor"					NVARCHAR(3)		NULL,
	PRIMARY KEY ("IdParametroFinanciero")
);

ALTER TABLE "BSCL.Plataforma.BD::MER.ParametroFinanciero" DROP ("Nombre");
ALTER TABLE "BSCL.Plataforma.BD::MER.ParametroFinanciero" DROP ("IdMoneda");
ALTER TABLE "BSCL.Plataforma.BD::MER.ParametroFinanciero" DROP ("Descripcion");
ALTER TABLE "BSCL.Plataforma.BD::MER.ParametroFinanciero" DROP ("Periodo");

ALTER TABLE "BSCL.Plataforma.BD::MER.ParidadYTipoCambio" ADD ("TipoCambioReferencial" DECIMAL  (26,8  ) NULL  );

insert into "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero"
SELECT 'UF','DI','UNIDAD DE FOMENTO','UNIDAD DE FOMENTO','UF','CLP'from dummy union all
SELECT 'UTM','ME','UNIDAD TRIBUTARIA MENSUAL','UNIDAD TRIBUTARIA MENSUAL','UTM','CLP'from dummy union all
SELECT 'ICP','DI','INDICE DE CAMARA PROMEDIO','INDICE DE CAMARA PROMEDIO','',''from dummy union all
SELECT 'IVP','DI','INDICE DE VALOR PROMEDIO','INDICE DE VALOR PROMEDIO','',''from dummy union all
SELECT 'LIBOR','DI','TASA LIBOR','TASA LIBOR','',''from dummy union all
SELECT 'PCD','DI','TASA TIP SVS PCD','TASA TIP SVS PCD','',''from dummy union all
SELECT 'PTF','DI','TASA TIP SVS PTF','TASA TIP SVS PTF','',''from dummy union all
SELECT 'TAB360','AN','TAB 360','TAB 360','',''from dummy union all
SELECT 'TAB90','ME','TAB 90','TAB 90','',''from dummy union all
SELECT 'IPC','ME','INDICE PRECIO CONSUMIDOR','INDICE PRECIO CONSUMIDOR','',''from dummy;
commit;