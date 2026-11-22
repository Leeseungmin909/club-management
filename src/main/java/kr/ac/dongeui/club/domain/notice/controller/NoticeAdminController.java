package kr.ac.dongeui.club.domain.notice.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.notice.service.NoticeService;

/** 공지 작성 · 수정 · 삭제 (SFR-13, 15). /admin/* 라서 AuthFilter 가 관리자만 들여보낸다. */
@WebServlet({"/admin/notices/write", "/admin/notices/edit", "/admin/notices/delete"})
public class NoticeAdminController extends HttpServlet {

    private static final String FORM = "/WEB-INF/views/notice/form.jsp";
    private final NoticeService noticeService = new NoticeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (req.getServletPath().equals("/admin/notices/edit")) {
                Map<String, Object> notice = noticeService.find(NoticeController.parseInt(req.getParameter("id"), -1));
                if (notice == null) {
                    NoticeController.showDeleted(req, resp);
                    return;
                }
                req.setAttribute("notice", notice);
            } else if (req.getServletPath().equals("/admin/notices/delete")) {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            req.getRequestDispatcher(FORM).forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        int id = NoticeController.parseInt(req.getParameter("id"), -1);
        String title = req.getParameter("title");
        String content = req.getParameter("content");
        boolean pinned = "true".equals(req.getParameter("pinned"));
        try {
            switch (path) {
                case "/admin/notices/write" -> {
                    Member me = (Member) req.getSession().getAttribute("me");
                    int newId = noticeService.write(me, title, content, pinned);
                    req.getSession().setAttribute("flash", "공지를 등록했어요.");
                    resp.sendRedirect(req.getContextPath() + "/notices/view?id=" + newId);
                }
                case "/admin/notices/edit" -> {
                    if (!noticeService.edit(id, title, content, pinned)) {
                        NoticeController.showDeleted(req, resp);
                        return;
                    }
                    req.getSession().setAttribute("flash", "공지를 수정했어요.");
                    resp.sendRedirect(req.getContextPath() + "/notices/view?id=" + id);
                }
                case "/admin/notices/delete" -> {
                    noticeService.delete(id);   // 이미 지워졌어도 목록으로 돌려보낸다
                    req.getSession().setAttribute("flash", "공지를 삭제했어요.");
                    resp.sendRedirect(req.getContextPath() + "/notices");
                }
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {   // 입력 오류: 쓰던 내용을 그대로 두고 안내
            req.setAttribute("error", e.getMessage());
            if (path.equals("/admin/notices/edit")) req.setAttribute("notice", Map.of("id", id, "title", title == null ? "" : title,
                    "content", content == null ? "" : content, "is_pinned", pinned));
            req.getRequestDispatcher(FORM).forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
