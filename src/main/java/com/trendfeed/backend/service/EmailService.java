package com.trendfeed.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@trendfeed.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Async
    public CompletableFuture<Boolean> sendNewsletterConfirmation(String toEmail, String confirmationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("TrendFeed - 뉴스레터 구독 확인");
            
            String confirmUrl = baseUrl + "/api/newsletter/confirm?token=" + confirmationToken;
            
            String htmlContent = buildConfirmationEmailHtml(confirmUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Confirmation email sent to: {}", toEmail);
            
            return CompletableFuture.completedFuture(true);
            
        } catch (MessagingException e) {
            log.error("Failed to send confirmation email to: {}", toEmail, e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    @Async
    public CompletableFuture<Boolean> sendNewsletterWelcome(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("TrendFeed - 구독해 주셔서 감사합니다!");
            
            String htmlContent = buildWelcomeEmailHtml();
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
            
            return CompletableFuture.completedFuture(true);
            
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    private String buildConfirmationEmailHtml(String confirmUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; }
                    .button { display: inline-block; padding: 12px 30px; background: #667eea; 
                             color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎨 TrendFeed</h1>
                        <p>뉴스레터 구독을 환영합니다!</p>
                    </div>
                    <div class="content">
                        <h2>이메일 주소를 확인해 주세요</h2>
                        <p>안녕하세요,</p>
                        <p>TrendFeed 뉴스레터 구독을 신청해 주셔서 감사합니다!</p>
                        <p>아래 버튼을 클릭하여 이메일 주소를 확인해 주세요:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">이메일 확인하기</a>
                        </div>
                        <p style="margin-top: 30px; font-size: 14px; color: #666;">
                            버튼이 작동하지 않으면 아래 링크를 복사하여 브라우저에 붙여넣으세요:<br>
                            <a href="%s">%s</a>
                        </p>
                        <p style="margin-top: 30px; color: #888; font-size: 12px;">
                            이 메일은 24시간 후 만료됩니다.<br>
                            요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2025 TrendFeed. All rights reserved.</p>
                        <p>매주 최신 트렌딩 오픈소스를 코믹으로 만나보세요 📚</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(confirmUrl, confirmUrl, confirmUrl);
    }
    
    private String buildWelcomeEmailHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; }
                    .feature { background: white; padding: 15px; margin: 15px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 환영합니다!</h1>
                        <p>TrendFeed 구독이 완료되었습니다</p>
                    </div>
                    <div class="content">
                        <h2>안녕하세요!</h2>
                        <p>TrendFeed 뉴스레터 구독을 확인해 주셔서 감사합니다.</p>
                        
                        <h3>매주 받아보실 내용:</h3>
                        
                        <div class="feature">
                            <strong>📈 트렌딩 오픈소스</strong>
                            <p>GitHub에서 가장 핫한 프로젝트들을 선별하여 전달합니다.</p>
                        </div>
                        
                        <div class="feature">
                            <strong>🎨 코믹 형식</strong>
                            <p>복잡한 기술 내용을 쉽고 재미있게 코믹으로 만나보세요.</p>
                        </div>
                        
                        <div class="feature">
                            <strong>💡 핵심 인사이트</strong>
                            <p>각 프로젝트의 핵심 기능과 사용법을 간단히 정리해 드립니다.</p>
                        </div>
                        
                        <p style="margin-top: 30px;">
                            다음 뉴스레터를 기대해 주세요! 🚀
                        </p>
                        
                        <p style="margin-top: 20px; font-size: 14px; color: #666;">
                            언제든지 구독을 취소하실 수 있습니다.
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2025 TrendFeed. All rights reserved.</p>
                        <p>매주 최신 트렌딩 오픈소스를 코믹으로 만나보세요 📚</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
    
    /**
     * 실제 뉴스레터 발송 (트렌딩 코믹 포함)
     */
    @Async
    public CompletableFuture<Boolean> sendNewsletter(String toEmail, String unsubscribeToken, 
                                                      java.util.List<com.trendfeed.backend.dto.response.ComicResponse> comics) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("TrendFeed 주간 뉴스레터 - 이번 주 핫한 오픈소스 프로젝트 🔥");
            
            String htmlContent = buildNewsletterHtml(comics, unsubscribeToken);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Newsletter sent to: {}", toEmail);
            
            return CompletableFuture.completedFuture(true);
            
        } catch (MessagingException e) {
            log.error("Failed to send newsletter to: {}", toEmail, e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    private String buildNewsletterHtml(java.util.List<com.trendfeed.backend.dto.response.ComicResponse> comics, 
                                        String unsubscribeToken) {
        StringBuilder comicsHtml = new StringBuilder();
        
        for (com.trendfeed.backend.dto.response.ComicResponse comic : comics) {
            String insights = comic.getKeyInsights() != null ? 
                String.join(", ", comic.getKeyInsights()) : "";
            
            comicsHtml.append(String.format("""
                <div class="comic-card">
                    <h3>%s ⭐ %d</h3>
                    <p class="language">%s</p>
                    <p class="insights"><strong>핵심 포인트:</strong> %s</p>
                    <a href="%s" class="button">GitHub에서 보기</a>
                </div>
                """, 
                comic.getRepoName(),
                comic.getStars(),
                comic.getLanguage() != null ? comic.getLanguage() : "Unknown",
                insights.isEmpty() ? "트렌딩 프로젝트" : insights,
                comic.getRepoUrl()
            ));
        }
        
        String unsubscribeUrl = baseUrl + "/api/newsletter/unsubscribe?token=" + unsubscribeToken;
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
                    .container { max-width: 700px; margin: 0 auto; background: white; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                             color: white; padding: 40px 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 32px; }
                    .header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; }
                    .content { padding: 30px; }
                    .intro { background: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 30px; }
                    .comic-card { background: white; border: 1px solid #e0e0e0; border-radius: 8px; 
                                 padding: 25px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .comic-card h3 { color: #667eea; margin-top: 0; }
                    .language { display: inline-block; background: #667eea; color: white; 
                               padding: 4px 12px; border-radius: 12px; font-size: 12px; margin: 10px 0; }
                    .description { color: #666; line-height: 1.8; }
                    .insights { background: #f0f4ff; padding: 12px; border-radius: 5px; 
                               border-left: 3px solid #667eea; }
                    .button { display: inline-block; padding: 10px 24px; background: #667eea; 
                             color: white; text-decoration: none; border-radius: 5px; margin-top: 10px; }
                    .button:hover { background: #5568d3; }
                    .footer { background: #f9f9f9; padding: 30px; text-align: center; 
                             border-top: 1px solid #e0e0e0; }
                    .footer p { margin: 5px 0; color: #888; font-size: 13px; }
                    .footer a { color: #667eea; text-decoration: none; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎨 TrendFeed</h1>
                        <p>이번 주 핫한 오픈소스 프로젝트</p>
                    </div>
                    
                    <div class="content">
                        <div class="intro">
                            <p><strong>안녕하세요!</strong></p>
                            <p>이번 주 GitHub에서 가장 주목받는 프로젝트들을 엄선하여 전달합니다. 
                               각 프로젝트의 핵심 내용을 빠르게 파악하고, 마음에 드는 것이 있다면 바로 확인해보세요! 🚀</p>
                        </div>
                        
                        %s
                        
                        <div style="margin-top: 40px; padding: 20px; background: #f0f4ff; border-radius: 8px; text-align: center;">
                            <p style="margin: 0;"><strong>더 많은 트렌딩 프로젝트를 확인하고 싶으신가요?</strong></p>
                            <p style="margin: 10px 0 0 0;">
                                <a href="%s" class="button">TrendFeed 웹사이트 방문하기</a>
                            </p>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>© 2025 TrendFeed. All rights reserved.</p>
                        <p style="margin-top: 15px;">
                            이 이메일을 더 이상 받고 싶지 않으신가요?<br>
                            <a href="%s">구독 취소</a>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, comicsHtml.toString(), baseUrl, unsubscribeUrl);
    }
}
