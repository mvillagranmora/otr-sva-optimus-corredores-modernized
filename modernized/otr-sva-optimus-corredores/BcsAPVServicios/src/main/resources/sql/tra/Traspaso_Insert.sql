INSERT INTO "BSCL.APV.BD::TRA.Traspaso"(
	 "IdTraspaso"				--BIGINT	NOT NULL,
	,"IdContratoSAAPV"			--BIGINT,
	,"IdEmpresa"				--BIGINT,
	,"TipoMovimiento"			--NVARCHAR(10)
	,"RazonRetiro"				--NVARCHAR(3)
	,"FechaIngreso"				--DATE,
	,"FechaEgreso"				--DATE,
	,"FechaModificacion"		--DATE,
	,"FechaSuscripcion"			--DATE,
	,"CodigoTipoTraspaso"		--NVARCHAR(10),
	,"IdCliente"				--BIGINT,
	,"IdPersona"				--BIGINT,
	,"Descripcion"				--NVARCHAR(255)
	,"CodigoTipoAhorro"			--NVARCHAR(10),	
	,"NombreEntidad"			--NVARCHAR(100),
	,"DocumentoEntidad"			--NVARCHAR(11),
	/*
	,"FondoOrigen"				--NVARCHAR(50),
	,"MontoTotalTraspaso"		--DECIMAL(26,8),
	,"UFTotalTraspaso"			--DECIMAL(26,8),
	,"PorcentajeTotalTraspaso"	--DECIMAL(26,8),
	*/
	,"IdEstado"					--NVARCHAR(3)
	,"Canal"					--NVARCHAR(100)
	,"CierreSolicitud"			--DATE
)
VALUES(
	 :IdTraspaso				--BIGINT	NOT NULL,
	,:IdContratoSAAPV			--BIGINT,
	,:IdEmpresa					--BIGINT,
	,:TipoMovimiento			--NVARCHAR(10)
	,:RazonRetiro				--NVARCHAR(3)
	,:FechaIngreso				--DATE,
	,:FechaEgreso				--DATE
	,:FechaModificacion			--DATE,
	,:FechaSuscripcion			--DATE,
	,:CodigoTipoTraspaso		--NVARCHAR(10),
	,:IdCliente					--BIGINT,
	,:IdPersona					--BIGINT,
	,:Descripcion				--NVARCHAR(255)
	,:CodigoTipoAhorro			--NVARCHAR(10),	
	,:NombreEntidad				--NVARCHAR(100),
	,:DocumentoEntidad			--NVARCHAR(11),
	/*
	,:FondoOrigen				--NVARCHAR(50),
	,:MontoTotalTraspaso		--DECIMAL(26,8),
	,:UFTotalTraspaso			--DECIMAL(26,8),
	,:PorcentajeTotalTraspaso	--DECIMAL(26,8),
	*/
	,:IdEstado					--NVARCHAR(3)
	,:Canal						--NVARCHAR(100)
	,:CierreSolicitud			--DATE,
);