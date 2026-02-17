package com.example.battleship.mapper;

import com.example.battleship.domain.map.Cell;
import com.example.battleship.dto.rest.outbound.CellResponse;

public class CellMapper {

    public static CellResponse toResponse(Cell cell, boolean isOwner) {

        CellResponse response = new CellResponse();

        response.setX(cell.getCoordinate().getX());
        response.setY(cell.getCoordinate().getY());
        response.setAttacked(cell.isAttacked());

        if (cell.isAttacked()) {
            response.setHit(cell.hasShip());
        }

        if (isOwner) {
            response.setHasShip(cell.hasShip());
        }

        return response;
    }
}

