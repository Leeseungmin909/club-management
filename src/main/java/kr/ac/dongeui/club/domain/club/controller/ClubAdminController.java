package kr.ac.dongeui.club.domain.club.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.club.entity.Club;
import kr.ac.dongeui.club.domain.club.repository.ClubRepository;
import kr.ac.dongeui.club.global.infra.file.LocalFileStorageService;

/** 동아리 정보 수정 (SFR-05): 이름 · 학교 · 소개 · 대표 이미지 · 배경 이미지. 관리자만. */
@WebServlet("/admin/club")
@MultipartConfig(maxFileSize = LocalFileStorageService.MAX_BYTES, maxRequestSize = 2 * LocalFileStorageService.MAX_BYTES + 1024 * 1024)
public class ClubAdminController extends HttpServlet {

    private final ClubRepository clubRepository = new ClubRepository();
    private final LocalFileStorageService fileStorage = new LocalFileStorageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/admin/club.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = strip(req.getParameter("name"));
        String school = strip(req.getParameter("school"));
        String intro = strip(req.getParameter("intro"));
        String newImage = null;
        String newBanner = null;
        try {
            if (name.isEmpty()) throw new IllegalArgumentException("동아리 이름을 입력해 주세요.");
            if (name.length() > 50) throw new IllegalArgumentException("동아리 이름은 50자 이하로 입력해 주세요.");
            if (school.length() > 50) throw new IllegalArgumentException("학교는 50자 이하로 입력해 주세요.");
            if (intro.length() > 500) throw new IllegalArgumentException("소개는 500자 이하로 입력해 주세요.");

            Club old = clubRepository.find();
            newImage = store(req.getPart("image"));
            newBanner = store(req.getPart("banner"));
            clubRepository.update(name, school.isEmpty() ? null : school, intro.isEmpty() ? null : intro, newImage, newBanner);
            if (old != null) {   // 바뀐 이미지의 예전 파일 정리
                if (newImage != null) fileStorage.delete(old.getImagePath());
                if (newBanner != null) fileStorage.delete(old.getBannerPath());
            }
            req.getSession().setAttribute("flash", "동아리 정보를 저장했어요.");
            resp.sendRedirect(req.getContextPath() + "/admin/club");
        } catch (IllegalArgumentException | IllegalStateException e) {   // 입력 오류 · 파일 형식 · 용량 초과
            fileStorage.delete(newImage);   // 저장은 안 됐으니 방금 올린 파일 정리
            fileStorage.delete(newBanner);
            req.setAttribute("error", e instanceof IllegalStateException ? "파일은 100MB 이하만 올릴 수 있어요." : e.getMessage());
            Club typed = new Club(name, school, intro, null, null);   // 쓰던 글자 유지 (이미지는 화면에서 다시 고르게)
            Club current = null;
            try {
                current = clubRepository.find();
            } catch (SQLException ignored) {
                // 오류 화면에서는 기존 이미지를 못 보여 줘도 된다
            }
            req.setAttribute("club", new Club(typed.getName(), typed.getSchool(), typed.getIntro(),
                    current == null ? null : current.getImagePath(), current == null ? null : current.getBannerPath()));
            req.getRequestDispatcher("/WEB-INF/views/admin/club.jsp").forward(req, resp);
        } catch (SQLException e) {
            fileStorage.delete(newImage);
            fileStorage.delete(newBanner);
            throw new ServletException(e);
        }
    }

    /** 파일을 골랐을 때만 저장한다. 고르지 않았으면 null (기존 이미지 유지) */
    private String store(Part part) throws IOException {
        return part == null || part.getSize() == 0 ? null : fileStorage.store(part, LocalFileStorageService.IMAGES);
    }

    private static String strip(String s) {
        return s == null ? "" : s.strip();
    }
}
