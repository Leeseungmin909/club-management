package kr.ac.dongeui.club.domain.club.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 동아리 소개 (모든 회원). 동아리 정보는 AuthFilter 가 request "club" 으로 넣어 준다. */
@WebServlet("/club")
public class ClubController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/club.jsp").forward(req, resp);
    }
}
