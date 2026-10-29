SET SCHEMA "BSCL";

DROP TABLE "BSCL.Plataforma.BD::COM_2.ClaseCobro";
DROP TABLE "BSCL.Plataforma.BD::COM_2.TipoCobro";
DROP TABLE "BSCL.Plataforma.BD::COM_2.EstadoPlataformaComercial";
DROP TABLE "BSCL.Plataforma.BD::COM_2.BaseTasaCobro";
DROP TABLE "BSCL.Plataforma.BD::COM_2.TipoRangoCobro";
DROP TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro";
--DROP TABLE "BSCL.Plataforma.BD::COM_2.DetallePerfilCobros";
--DROP TABLE "BSCL.Plataforma.BD::COM_2.DetallePerfilProductos";
--DROP TABLE "BSCL.Plataforma.BD::COM_2.PerfilCobros";
--DROP TABLE "BSCL.Plataforma.BD::COM_2.PerfilProductos";

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.ClaseCobro" (
	"IdClaseCobro"			NVARCHAR(40) NOT NULL,
	"NombreClaseCobro"		NVARCHAR(100) NOT NULL,
	PRIMARY KEY ("IdClaseCobro")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.TipoCobro" (
	"IdTipoCobro"				NVARCHAR(40) NOT NULL,
	"NombreTipoCobro"			NVARCHAR(100) NOT NULL,
	"PermiteMontoFijo"			NVARCHAR(1) NOT NULL DEFAULT 'S',
	"PermitePorcentajeFijo"		NVARCHAR(1) NOT NULL DEFAULT 'S',
	"PermitePorcentajeRangos"	NVARCHAR(1) NOT NULL DEFAULT 'S',
	PRIMARY KEY ("IdTipoCobro")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.EstadoPlataformaComercial" (
	"IdEstadoPlataformaComercial"	BIGINT,
	"IdEstado"						NVARCHAR(100) NOT NULL,
	"NombreEntidad" 				NVARCHAR(100) NOT NULL,
	PRIMARY KEY ("IdEstadoPlataformaComercial")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.BaseTasaCobro" (
	"IdBaseTasaCobro"		BIGINT,
	"NombreBaseTasaCobro"	NVARCHAR(100) NOT NULL,
	"ValorBaseTasaCobro" 	DECIMAL(26,8) NOT NULL,
	PRIMARY KEY ("IdBaseTasaCobro")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.TipoRangoCobro" (
	"IdTipoRangoCobro"		NVARCHAR(40),
	"NombreTipoRangoCobro"	NVARCHAR(100) NOT NULL,
	PRIMARY KEY ("IdTipoRangoCobro")
);

--SELECT * FROM "BSCL"."BSCL.Plataforma.BD::COM_2.DefinicionCobro"
CREATE COLUMN TABLE "BSCL.Plataforma.BD::COM_2.DefinicionCobro" (
	"IdEmpresa"								BIGINT NOT NULL,
	"IdClaseCobro"							NVARCHAR(40) NOT NULL,
	"IdTipoCobro"							NVARCHAR(40) NOT NULL,
	"CodigoDefinicionCobro"					NVARCHAR(255) NOT NULL,
	"IdEstado"								NVARCHAR(3) NOT NULL,
	"Descripcion"							NVARCHAR(5000) NOT NULL DEFAULT '',
	
	"CheckCobroMonedaOperacion"				NVARCHAR(1) NOT NULL DEFAULT 'S',
	"IdMonedaCobroOtraMoneda"				NVARCHAR(3) NULL,
	
	--PARA MONTO FIJO
	"MontoFijo_MontoCobro"					DECIMAL(26,8) NOT NULL DEFAULT 0,
	"MontoFijo_CheckMontoOperacionMenor"	NVARCHAR(1) NOT NULL DEFAULT 'N',
	"MontoFijo_ValorMontoOperacionMenor"	DECIMAL(26,8) NOT NULL DEFAULT 0,
	"MontoFijo_CheckSiNo"					NVARCHAR(1) NOT NULL DEFAULT 'N',
	"MontoFijo_ValorMontoSiNo"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	
	--PARA PORCENTUAL FIJO
	"PorcentualFijo_ValorCobro"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PorcentualFijo_CheckSobreMonto"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_CheckSobreCantidad"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_CheckMayorEntreDos"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_CheckMenorEntreDos"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_CheckMontoMinimo"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_ValorMontoMinimo"		DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PorcentualFijo_CheckMontoMaximo"		NVARCHAR(1)	NOT NULL DEFAULT 'N',
	"PorcentualFijo_ValorMontoMaximo"		DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PorcentualFijo_IdMonedaMinimoMaximo"	NVARCHAR(3) NULL,
	
	--OTROS DATOS
	"Otros_CheckOperacionContado"			NVARCHAR(1) NOT NULL DEFAULT 'S',
	"Otros_CheckOperacionPlazo"				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckBaseTasa"					NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_ValorBaseTasa"					BIGINT NULL,
	"Otros_CheckInteresSimple"				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckInteresCompuesto"			NVARCHAR(1) NOT NULL DEFAULT 'N',
	
	"Otros_CheckAllIn"						NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_ValorAllInMontoOperacionBase"	DECIMAL(26,8) NOT NULL DEFAULT 0,
	
	"Otros_CheckAfectoImpuesto"				NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckAplicaPortafolio"			NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckAplicaCorresponsal"			NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckPermiteAumentarValor"		NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Otros_CheckPermiteDisminuirValor"		NVARCHAR(1) NOT NULL DEFAULT 'N',
	
	--PARA RANGOS

	--PARA EXCLUSIONES
	
	
	PRIMARY KEY ("IdEmpresa","CodigoDefinicionCobro")
);








