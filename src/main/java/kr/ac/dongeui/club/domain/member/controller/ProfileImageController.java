package kr.ac.dongeui.club.domain.member.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.service.MemberService;
import kr.ac.dongeui.club.global.infra.file.LocalFileStorageService;

/** 프로필 이미지 변경 (SFR-12) */
@WebServlet("/me/photo")
@MultipartConfig(maxFileSize = LocalFileStorageService.MAX_BYTES, maxRequestSize = LocalFileStorageService.MAX_BYTES + 1024 * 1024)
public class ProfileImageController extends HttpServlet {

    private final MemberService memberService = new MemberService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member me = (Member) req.getSession().getAttribute("me");
        String flash;
        try {
            memberService.changeProfileImage(me, req.getPart("photo"));
            flash = "프로필 사진을 바꿨어요.";
        } catch (IllegalArgumentException e) {
            flash = e.getMessage();
        } catch (IllegalStateException e) {  // @MultipartConfig 용량 초과
            flash = "파일은 100MB 이하만 올릴 수 있어요.";
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getSession().setAttribute("flash", flash);
        resp.sendRedirect(req.getContextPath() + "/me");
    }
}
