-- 샘플 데이터 삽입 (멱등성 보장)
-- ON CONFLICT DO NOTHING을 사용하여 중복 삽입 방지

-- 샘플 코믹 데이터
INSERT INTO comics (id, repo_name, repo_url, stars, language, panels, key_insights, is_new, likes, shares, comments, created_at, updated_at)
VALUES 
  (1, 'shadcn/ui', 'https://github.com/shadcn/ui', 45230, 'TypeScript', 
   '[]'::jsonb, 
   '["Beautiful UI components", "Built with Radix UI", "Highly customizable"]'::jsonb, 
   true, 1234, 89, 56, NOW(), NOW()),
  
  (2, 'vercel/next.js', 'https://github.com/vercel/next.js', 120000, 'JavaScript', 
   '[]'::jsonb, 
   '["React framework", "Server-side rendering", "Static site generation"]'::jsonb, 
   true, 5000, 200, 100, NOW(), NOW()),
  
  (3, 'facebook/react', 'https://github.com/facebook/react', 220000, 'JavaScript', 
   '[]'::jsonb, 
   '["UI library", "Component-based", "Virtual DOM"]'::jsonb, 
   false, 8000, 500, 300, NOW(), NOW()),
  
  (4, 'vuejs/core', 'https://github.com/vuejs/core', 45000, 'TypeScript', 
   '[]'::jsonb, 
   '["Progressive framework", "Easy to learn", "Reactive data binding"]'::jsonb, 
   true, 3000, 150, 80, NOW(), NOW()),
  
  (5, 'tailwindlabs/tailwindcss', 'https://github.com/tailwindlabs/tailwindcss', 78000, 'JavaScript', 
   '[]'::jsonb, 
   '["Utility-first CSS", "Highly customizable", "JIT compiler"]'::jsonb, 
   true, 4500, 180, 120, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- SERIAL 시퀀스 재조정 (다음 ID가 6부터 시작하도록)
SELECT setval('comics_id_seq', (SELECT COALESCE(MAX(id), 0) FROM comics) + 1, false);

-- 샘플 데이터 삽입 완료 로그
DO $$ 
BEGIN
    RAISE NOTICE 'Sample comic data inserted successfully!';
END $$;
